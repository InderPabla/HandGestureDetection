
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation, Flatten, Merge
from keras.layers import Convolution2D, ZeroPadding2D, MaxPooling2D
import time
import numpy as np

def custom_model_1():
    '''
    USER CODE STARTS HERE
    '''
    image_model = Sequential()
    image_model.add(ZeroPadding2D((2, 2), batch_input_shape=(1, 3, 50, 50)))   
   
    #54x54 fed in due to zero padding
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_2'))
    
    image_model.add(MaxPooling2D((2, 2), strides=(2, 2))) #convert 50x50 to 25x25
        
    #25x25 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_2'))
    
    image_model.add(MaxPooling2D((5, 5), strides=(5, 5))) #convert 25x25 to 5x5
    
    #5x5 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(40, 5, 5, activation='relu', name='conv3_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(32, 5, 5, activation='relu', name='conv3_2'))
    
    image_model.add(Dropout(0.25))
    
    image_model.add(Flatten())

      
    multi_layer_model = Sequential()  
    
    multi_layer_model.add(Dense(512, batch_input_shape=(1, 1)))
    multi_layer_model.add(Activation('tanh'))
    multi_layer_model.add(Dense(512))
    multi_layer_model.add(Activation('tanh'))
    multi_layer_model.add(Dropout(0.25))
    
    merged = Merge([image_model, multi_layer_model], mode='concat')

    final_model = Sequential()
    final_model.add(merged)
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    final_model.add(Dropout(0.25))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    final_model.add(Dropout(0.25))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    final_model.add(Dropout(0.25))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(3))
    final_model.add(Activation('sigmoid'))  
    
    return final_model
    '''
    USER MODE ENDS HERE
    '''
def custom_model_1_2():
    '''
    USER CODE STARTS HERE
    '''
    image_model = Sequential()
    image_model.add(ZeroPadding2D((2, 2), batch_input_shape=(1, 3, 50, 50)))   
   
    #54x54 fed in due to zero padding
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_2'))
    
    image_model.add(MaxPooling2D((2, 2), strides=(2, 2))) #convert 50x50 to 25x25
        
    #25x25 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_2'))
    
    image_model.add(MaxPooling2D((5, 5), strides=(5, 5))) #convert 25x25 to 5x5
    
    #5x5 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(40, 5, 5, activation='relu', name='conv3_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(32, 5, 5, activation='relu', name='conv3_2'))
    
    image_model.add(Dropout(0.25))
    
    image_model.add(Flatten())

      
    multi_layer_model = Sequential()  
    
    multi_layer_model.add(Dense(10, batch_input_shape=(1, 1)))
    multi_layer_model.add(Activation('tanh'))
    
    merged = Merge([image_model, multi_layer_model], mode='concat')

    final_model = Sequential()
    final_model.add(merged)
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    final_model.add(Dropout(0.25))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    final_model.add(Dropout(0.25))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    final_model.add(Dropout(0.25))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(3))
    final_model.add(Activation('sigmoid'))  
    
    return final_model
    '''
    USER MODE ENDS HERE
    '''

def custom_model_1_3():
    '''
    USER CODE STARTS HERE
    '''
    image_model = Sequential()
    image_model.add(ZeroPadding2D((2, 2), batch_input_shape=(1, 3, 50, 50)))   
   
    #54x54 fed in due to zero padding
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_2'))
    
    image_model.add(MaxPooling2D((2, 2), strides=(2, 2))) #convert 50x50 to 25x25
        
    #25x25 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_2'))
    
    image_model.add(MaxPooling2D((5, 5), strides=(5, 5))) #convert 25x25 to 5x5
    
    #5x5 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(40, 5, 5, activation='relu', name='conv3_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(32, 5, 5, activation='relu', name='conv3_2'))
    
    image_model.add(Dropout(0.25))
    
    image_model.add(Flatten())

      
    multi_layer_model = Sequential()  
    
    multi_layer_model.add(Dense(512, batch_input_shape=(1, 10)))
    multi_layer_model.add(Activation('tanh'))
    
    merged = Merge([image_model, multi_layer_model], mode='concat')

    final_model = Sequential()
    final_model.add(merged)
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    final_model.add(Dropout(0.25))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    final_model.add(Dropout(0.25))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    #final_model.add(Dropout(0.25))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(512))
    final_model.add(Activation('tanh'))
    
    final_model.add(Dense(3))
    final_model.add(Activation('sigmoid'))  
    
    return final_model
    '''
    USER MODE ENDS HERE
    '''
    
def custom_model_2():
    image_model = Sequential()
    image_model.add(ZeroPadding2D((2, 2), batch_input_shape=(1, 3, 100, 56)))   

    image_model.add(Convolution2D(10, 5, 5, activation='relu', name='conv1_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(10, 5, 5, activation='relu', name='conv1_2'))
    
    image_model.add(MaxPooling2D((2, 2), strides=(2, 2))) 
        

    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(20, 5, 5, activation='relu', name='conv2_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(20, 5, 5, activation='relu', name='conv2_2'))
    
    image_model.add(MaxPooling2D((5, 4), strides=(5, 4))) 
    
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(40, 5, 5, activation='relu', name='conv3_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(40, 5, 5, activation='relu', name='conv3_2'))
    
    
    image_model.add(Flatten())
    print(image_model.output_shape)
    image_model.add(Dense(2048))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(2048))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(2048))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(1024))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(1024))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(1024))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(1024))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(1024))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(1024))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(8))
    image_model.add(Activation('sigmoid'))  
    
    return image_model
   
def custom_cancer_model():
    cancer_model = Sequential()
    
    cancer_model.add(Dense(2048, input_dim=9))
    cancer_model.add(Activation('sigmoid'))
    
    cancer_model.add(Dense(2048))
    cancer_model.add(Activation('sigmoid'))
    
    cancer_model.add(Dense(1024))
    cancer_model.add(Activation('sigmoid'))
    
    cancer_model.add(Dense(1024))
    cancer_model.add(Activation('sigmoid'))
    
    cancer_model.add(Dense(1024))
    cancer_model.add(Activation('sigmoid'))

    cancer_model.add(Dense(2))
    cancer_model.add(Activation('sigmoid'))  
    
    return cancer_model

def custom_model_hand():
    '''
    USER CODE STARTS HERE
    '''
    image_model = Sequential()
    image_model.add(ZeroPadding2D((2, 2), batch_input_shape=(1, 3, 50, 50)))   
   
    #54x54 fed in due to zero padding
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_2'))
    
    image_model.add(MaxPooling2D((2, 2), strides=(2, 2))) #convert 50x50 to 25x25
        
    #25x25 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_2'))
    
    image_model.add(MaxPooling2D((5, 5), strides=(5, 5))) #convert 25x25 to 5x5
    
    #5x5 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(40, 5, 5, activation='relu', name='conv3_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(32, 5, 5, activation='relu', name='conv3_2'))
    
    image_model.add(Dropout(0.25))
    
    image_model.add(Flatten())

    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    image_model.add(Dropout(0.25))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    image_model.add(Dropout(0.2))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    image_model.add(Dropout(0.1))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(8))
    image_model.add(Activation('sigmoid'))  
    
    return image_model
    '''
    USER MODE ENDS HERE
    '''
 
def custom_model_hand_2():
    '''
    USER CODE STARTS HERE
    '''
    image_model = Sequential()
    image_model.add(ZeroPadding2D((2, 2), batch_input_shape=(1, 1, 50, 50)))   
   
    #54x54 fed in due to zero padding
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(8, 5, 5, activation='relu', name='conv1_2'))
    
    image_model.add(MaxPooling2D((2, 2), strides=(2, 2))) #convert 50x50 to 25x25
        
    #25x25 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(16, 5, 5, activation='relu', name='conv2_2'))
    
    image_model.add(MaxPooling2D((5, 5), strides=(5, 5))) #convert 25x25 to 5x5
    
    #5x5 fed in
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(40, 5, 5, activation='relu', name='conv3_1'))
    image_model.add(ZeroPadding2D((2, 2)))
    image_model.add(Convolution2D(32, 5, 5, activation='relu', name='conv3_2'))
    
    image_model.add(Dropout(0.2))
    
    image_model.add(Flatten())

    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    image_model.add(Dropout(0.2))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    image_model.add(Dropout(0.15))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    image_model.add(Dropout(0.1))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(512))
    image_model.add(Activation('tanh'))
    
    image_model.add(Dense(10))
    image_model.add(Activation('sigmoid'))

    return image_model
   
def make_model(file):
    print("==================================================") 
    
    print("Creating Model At: ",file) 
    start_time = time.time()
    model = custom_model_hand_2()    
    
    json_model = model.to_json()
    
    with open(file, "w") as json_file:
        json_file.write(json_model)
    
    end_time = time.time()
    total_time = end_time-start_time
    print("Model Created: ",total_time, " seconds")
    
    print("==================================================")
    

if __name__ == "__main__":   
    #make_model("cancer_model.json")
    make_model("hand_detection_model_3_visual.json")
    q = np.zeros((1,4));
    q[0][0]= 0
    q[0][1]= 1
    q[0][2]= 2
    q[0][3]= 3
    
    
    y = np.zeros((1,4));
    
    print(y)
    print(q)
    y[:] = q[:]
    y[0][3] = 4
    
    print(y)
    print(q)
    
    
    
    
    
    
    
    
    
    
    
    
    