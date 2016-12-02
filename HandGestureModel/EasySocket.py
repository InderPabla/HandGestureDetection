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

    def connect(self):
        self.client_socket.connect((self.host,self.port))
    
    def close(self):
        self.client_socket.close()
     
    def add_unpack_type(self,unpack_type):
        self.preset_unpack_types.append(unpack_type)
        
    def get_array_data(self,expected_length= 0,unpack_index = 0):
        
        if (expected_length > 0) and ((len(self.preset_unpack_types) - 1) <= unpack_index):

           packed_data = []
           packed_data = self.client_socket.recv(expected_length)
           print(packed_data)
           unpacked_data = struct.unpack(self.preset_unpack_types[unpack_index], bytearray(bytes(packed_data)))
         
           return unpacked_data
        else:
            return []
            
    def get_anything(self,expected_length= 0,unpack_index = 0):
        
        if (expected_length > 0) and ((len(self.preset_unpack_types) - 1) <= unpack_index):

           packed_data = []
           packed_data = self.client_socket.recv(expected_length)
           
           return True
    
        return False
    
    def send_string_data(self,message):
        self.client_socket.send(message.encode())
        
    def get_string(self, size):
        self.client_socket.recv(size)    
        
    