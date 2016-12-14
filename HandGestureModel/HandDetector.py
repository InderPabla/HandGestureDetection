import NetLoader as NetLoader
import DataLoader as DataLoader
import EasySocket as EasySocket
import numpy as np

def index_of_largest_element(array):
    large_index = 0
    large_element = array[0]
    for i in range(1,len(array)):
        if array[i]>large_element:
            large_element = array[i]
            large_index = i
    return large_index
        
if __name__ == "__main__":
    model_path = "hand_detection_model_3_visual.json"
    weights_path_2 = "hand_detection_weights_2.h5"
    weights_path = "hand_detection_weights_3.h5"
    weights_path_visual = "hand_detection_weights_3_visual.h5"
    res_x = 50
    res_y = 50
    image_file = "../Tests/AdvancedCarModel/real_time.png"
    raw_input_size = 0
    raw_output_size = 10
    
    train_mode = False
    prediction_mode = False
    
    image_val_data_location_1_1 = "HandGestureData/Ack2/"
    image_val_data_location_2_1 = "HandGestureData/Fist2/"
    image_val_data_location_3_1 = "HandGestureData/Hand2/"
    image_val_data_location_4_1 = "HandGestureData/One2/"
    image_val_data_location_5_1 = "HandGestureData/Straight2/"
    image_val_data_location_6_1 = "HandGestureData/Palm2/"
    image_val_data_location_7_1 = "HandGestureData/Thumbs2/"
    image_val_data_location_8_1 = "HandGestureData/None/"
    image_val_data_location_9_1 = "HandGestureData/Swing2/"
    image_val_data_location_1_2 = "HandGestureData/Ack/"
    image_val_data_location_2_2 = "HandGestureData/Fist/"
    image_val_data_location_3_2 = "HandGestureData/Hand/"
    image_val_data_location_4_2 = "HandGestureData/One/"
    image_val_data_location_5_2 = "HandGestureData/Straight/"
    image_val_data_location_6_2 = "HandGestureData/Palm/"
    image_val_data_location_7_2 = "HandGestureData/Thumbs/"
    image_val_data_location_8_2 = "HandGestureData/None2/"
    image_val_data_location_9_2 = "HandGestureData/Swing/"
    
    
    net = NetLoader.NetLoader(model_file=model_path, weights_file=weights_path_visual,
                              learning_rate = 0.001,decay_rate=0.00000001,
                              create_file=True,epoch_save = 1,copy=True,copy_path = weights_path)
   

    data_list_1 = [ # First data set (Rotational Gestures)
                   image_val_data_location_1_1,image_val_data_location_2_1, 
                   image_val_data_location_3_1,image_val_data_location_4_1,
                   image_val_data_location_5_1,image_val_data_location_6_1,
                   image_val_data_location_7_1,image_val_data_location_8_1,
                   image_val_data_location_9_1,
                   
                   # Second data set (Positional Gestures)
                   image_val_data_location_1_2,image_val_data_location_2_2,
                   image_val_data_location_3_2,image_val_data_location_4_2,
                   image_val_data_location_5_2,image_val_data_location_6_2,
                   image_val_data_location_7_2,image_val_data_location_8_2,
                   image_val_data_location_9_2]

    if(train_mode == False):
        data_list_1 = []
        
    data = DataLoader.DataLoader(data_list_1, size_x = res_x,
                                 size_y=res_y, num_inputs=raw_input_size, 
                                 num_outputs=raw_output_size,black_white=True)
    
    
    if(train_mode == True):
        
        data.combine_data(random_sort= True)
        input_element_1, output_element_1 = data.get_set_elements_to_train(0)
    
        if(prediction_mode == True):
            pre = net.predict(input_element_1[0])
            for i in range(0,len(pre)):
                pred = pre[i]*100
                print("Max Index: "+str(np.argmax(pred))+"  Output: "+str(int(pred[0]))+" "+str(int(pred[1]))+" "+str(int(pred[2]))+" "+str(int(pred[3]))+" "+str(int(pred[4])))
        else:    
            for i in range(0,15):
                net.fit(input_element_1[0],output_element_1,verbose = 2)
    else: 
        socket = EasySocket.EasySocket(preset_unpack_types = ['i'])
        socket.connect()
        while True:
            if(socket.get_anything(4,0) == True): 
                
                raw_RGB = data.load_image(image_file)
                raw_RGB = np.array(raw_RGB,dtype = np.float32)
                pre = net.predict(np.array([raw_RGB]))
                
                message = ""
                for i in range(0,len(pre[0])):
                    message +=str(pre[0][i])
                    if(i == len(pre[0])-1):
                        message+="\n"
                    else:
                        message+=" "
    
                socket.send_string_data(message)
        socket.close()
