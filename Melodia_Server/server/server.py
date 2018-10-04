# -*- coding:utf-8 -*- 
# ! usr/bin/python

from socket import *
import time
import threading
import os
import md5
import warnings

Host = ''
Port = 2017
Addr = (Host, Port)
midi_dict = {}

warnings.filterwarnings("ignore")


def md5_encode(src):
    m1 = md5.new()
    m1.update(src)
    return m1.hexdigest()


def tcplink(sock, addr):
    sessnum = 0
    music_data = ''
    while True:
        data = sock.recv(1480)
        if data[-9:]=='endbidou1':
            print 'wav recv finished'
            music_data+=data
            music_data=music_data[:-9]
            midi_data = eval(music_data)
	    sessnum = midi_data['request']  
            if midi_data['request'] == 1:
                flag_md5 = md5_encode(str(time.time()))
                print 'md5: ', flag_md5
                wav_name = flag_md5 + '.wav'
                with open(wav_name, 'w+') as f:
                    f.write(midi_data['data'].decode('base64'))
                    f.close()
                n = midi_data['config']['n'];
                m = midi_data['config']['m'];
                w = midi_data['config']['w'];
                midi_name = flag_md5 + '.mid'
                with open(midi_name, 'w') as f:
                    f.close()
                shellmid = '../mldm/hum2midi.py -n '+str(n)+' -m '+str(m)+' -w '+str(w)+' -o ' + midi_name + ' ' + wav_name
		print "running wav2midi shell"
                retmid = os.system(shellmid)
                retmid >= 8
                if retmid == 0:
	   	    print 'generate midi successful'
		    shellpng = 'mono ../mlds/sheet '+midi_name+' '+flag_md5
		    retpng = os.system(shellpng)
		    if retpng == 0:
                        sock.send(flag_md5.encode())
                        print 'generate png successful'
                        midi_dict[flag_md5] = midi_name
                        break
	 	    else:
			print 'generate png error'
			break
                else:
                    print 'generate midi error'
                    break
            elif midi_data['request'] == 2:
                flag = midi_data['data']
                if flag in midi_dict.keys():
                    fo = open(flag+'.mid', 'rb')
                    while True:
                        filedata = fo.read(1024)
                        if not filedata:
                            break
                        sock.send(filedata)
	 	    print 'midi file sent'
                    fo.close()
                    break
                else:
                    print 'can not find midi'
                    break
            else:
                print 'json error'
        else:
            music_data += data
    sock.close()
    print 'session '+str(sessnum)+' for '+str(addr)+' finished'

tcpSerSock = socket(AF_INET, SOCK_STREAM)
tcpSerSock.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
tcpSerSock.bind(Addr)
tcpSerSock.listen(5)

while True:
    tcpCliSock, tcpCliAddr = tcpSerSock.accept()
    print 'add ', tcpCliAddr
    t = threading.Thread(target=tcplink, args=(tcpCliSock, tcpCliAddr))
    t.start()
tcpSerSock.close()

