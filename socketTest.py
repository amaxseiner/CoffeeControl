import socket
import time
import RPi.GPIO as GPIO
import Adafruit_DHT 
import threading
import struct


temp = 0

def doTemp():
	global temp
	tempTime = 0
	while True:
		if ((time.time() - tempTime) > 2):
			humidity, temp = Adafruit_DHT.read_retry(Adafruit_DHT.AM2302, 4)
			tempTime = time.time()
	

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(18,GPIO.OUT)
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
host = 'zeus.vse.gmu.edu'
port = 5020
state = 0
count = 0
start = 0
once = 0
my_bytes = bytearray()
my_bytes.append(255)
my_bytes.append(255)
my_bytes.append(2) # the id of the PI
my_bytes.append(2) # request state
s.connect((host,port))
daThread = threading.Thread(name = 'daThread',target=doTemp)
daThread.start()
while True:
	try:
		#s.connect((host,port))
		if(state == 0 or state == 5):
			s.send(my_bytes)
			start = 0
		else:
			if(start == 0):
				start = time.time()
			print(state)
			print(str(time.time() - start))
			sendBytes = bytearray()
			sendBytes.append(255)
			sendBytes.append(255)
			sendBytes.append(2)
			if((time.time() - start) > 20):
				sendBytes.append(9)
				state = 9
			else:
				sendBytes.append(3)
			sendBytes.append(int(temp))
			print(int(temp))
			s.send(sendBytes)
		if(state == 1):
			GPIO.output(18,GPIO.HIGH)
		else:
			GPIO.output(18,GPIO.LOW)
		data = s.recv(1024)
		if count == 255:
			count = 0
		if data:
			if(ord(data[0]) == 255 and ord(data[1]) == 255):
				print ord(data[2])
				state = ord(data[2])
				#count = count +1
	except socket.error:
		print("keep gonig")
		#s.connect((host,port))
	time.sleep(0.3)
