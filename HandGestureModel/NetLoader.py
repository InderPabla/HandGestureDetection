"""
NetLoader.py
Deals with loading models file, traning and prediction.
NetLoader is using a libary called Keras which MUST BE installed in order for 
NetLoader to function. 
"""

from keras.models import model_from_json
from keras.optimizers import SGD, RMSprop
from keras.utils.visualize_util import plot
import os.path

class NetLoader:
    
    def __init__(self, model_file = "", weights_file = "", learning_rate = 0.01, 
                 decay_rate = 0.000001, loss_function = "mean_squared_error", 
                 momentum= 0.9, nesterov=True, train_mode = True, epoch_save = 25, optimizer = "SGD",create_file=False, copy=False, copy_path = ""):

        self.model_file = model_file
        self.weights_file = weights_file
        self.learning_rate= learning_rate
        self.decay_rate = decay_rate
        self.loss_function = loss_function
        self.momentum = momentum
        self.nesterov = nesterov
        self.train_mode = train_mode
        self.epoch_save = epoch_save
        
        self.model = None
        self.optimizer = optimizer
        
        self.create_file = create_file
            
        self.epoch_save_counter = 0
        self.save_counter = 0
        
        self.copy_path= copy_path
        self.copy = copy
        
        # if model file is given
        if not model_file == "":
            self.load_model() #load model

            # if copy path is given and copy is true
            if(not self.copy_path == "") and self.copy==True:
                # copy weights from copy path to given weights file
                self.model.load_weights(self.copy_path)
                self.model.save_weights(self.weights_file)

            # if weights file is given and it exists
            if (not weights_file  == "") and os.path.exists(weights_file):
                self.load_weights() # load weights
            
            # otherwise create a new weights file
            elif(self.create_file == True):
                self.model.save_weights(self.weights_file)
                
        # if train mode is true
        if self.train_mode == True:
            self.compile_model() #add optimizer to the model
    
    '''
    Load model from model file 
    '''
    def load_model(self):
        json_file = open(self.model_file, 'r')
        loaded_model_json = json_file.read()
        json_file.close()
        
        self.model = model_from_json(loaded_model_json)
        plot(self.model, to_file='model.png', show_shapes= True, show_layer_names = True)
    
    '''
    Load weights into the model from the weights file
    '''    
    def load_weights(self):
        self.model.load_weights(self.weights_file)
    
    '''
    Compile model with an optimizer which will used for traning
    '''  
    def compile_model(self):
        if(self.optimizer == "SGD"):
            self.optimizer = SGD(lr=self.learning_rate, decay=self.decay_rate, 
                                 momentum=self.momentum, nesterov=self.nesterov)
        elif(self.optimizer == "RMSProp"):              
            self.optimizer = RMSprop(lr=self.learning_rate, rho=0.9, epsilon=1e-08)
                                 
        self.model.compile(loss = self.loss_function, 
                           optimizer = self.optimizer)
    
    '''
    Using the given inputs calculate and return the predictions
    '''  
    def predict(self,inputs):
        prediction = self.model.predict(inputs)
        return prediction
    
    '''
    Traning the network with given inputs and outputs
    '''  
    def fit(self,inputs,outputs,epochs = 1,verbose = 0):
        self.model.fit(inputs, outputs, nb_epoch=epochs,verbose = verbose)
        self.epoch_save_counter = self.epoch_save_counter + epochs
        
        if self.epoch_save_counter >= self.epoch_save:
            self.epoch_save_counter = 0
            
            if not self.weights_file == "":
                self.model.save_weights(self.weights_file)
                self.save_counter = self.save_counter + 1
                print("Saving Counter: ",self.save_counter)
            
            
            
            
            
    