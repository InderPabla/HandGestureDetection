"""
EastSocket.py
Basic client side socket communication wtih a server. 
Can recieve and send data over socket. 
"""

import socket
import struct

class EasySocket:
    def __init__(self, host='localhost', port = 12345, preset_unpack_types = []):
        
        self.host = host
        self.port = port
        
        self.preset_unpack_types = []
        for i in range(0,len(preset_unpack_types)):
            self.preset_unpack_types.append(preset_unpack_types[i])
        
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    """
    Connect to server
    """
    def connect(self):
        self.client_socket.connect((self.host,self.port))
    
    """
    Close connection
    """
    def close(self):
        self.client_socket.close()
     
    """
    Adding unpack types 
    Suppose ['ii', 'iii'] is added 
    When the user uses index 0, they are expecting 2 integer to be retrieved from the server. 
    When the user uses index 1, they are expecting 3 integers to be retrieved from the server.
    """
    def add_unpack_type(self,unpack_type):
        self.preset_unpack_types.append(unpack_type)
     
    """
    Get data of the expected lenght with a unpack type index
    """
    def get_array_data(self,expected_length= 0,unpack_index = 0):
        
        if (expected_length > 0) and ((len(self.preset_unpack_types) - 1) <= unpack_index):

           packed_data = []
           packed_data = self.client_socket.recv(expected_length)
           print(packed_data)
           unpacked_data = struct.unpack(self.preset_unpack_types[unpack_index], bytearray(bytes(packed_data)))
         
           return unpacked_data
        else:
            return []
            
    """
    Get anything, doesn't really matter what
    """       
    def get_anything(self,expected_length= 0,unpack_index = 0):
        
        if (expected_length > 0) and ((len(self.preset_unpack_types) - 1) <= unpack_index):

           packed_data = []
           packed_data = self.client_socket.recv(expected_length)
           
           return True
    
        return False
    
    """
    Send string to server
    """
    def send_string_data(self,message):
        self.client_socket.send(message.encode())
     
    """
    Get string from server
    """
    def get_string(self, size):
        self.client_socket.recv(size)    
        
    