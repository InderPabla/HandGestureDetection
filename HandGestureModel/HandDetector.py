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
    model_path = "hand_detection_model_1.json"
    weights_path = "hand_detection_weights_1.h5"
    res_x = 50
    res_y = 50
    image_file = "../Tests/AdvancedCarModel/real_time.png"
    raw_input_size = 0
    raw_output_size = 8

    image_val_data_location_1 = "HandGestureData/Ack/"
    image_val_data_location_2 = "HandGestureData/Fist/"
    image_val_data_location_3 = "HandGestureData/Hand/"
    image_val_data_location_4 = "HandGestureData/One/"
    image_val_data_location_5 = "HandGestureData/Straight/"
    image_val_data_location_6 = "HandGestureData/Palm/"
    image_val_data_location_7 = "HandGestureData/Thumbs/"
    image_val_data_location_8 = "HandGestureData/None/"
    
    net = NetLoader.NetLoader(model_file=model_path,weights_file=weights_path,learning_rate = 0.001,decay_rate=0.00000001,create_file=False,epoch_save = 1)
   

    data_list_1 = [image_val_data_location_1,image_val_data_location_2,image_val_data_location_3,image_val_data_location_4,image_val_data_location_5,image_val_data_location_6,image_val_data_location_7,image_val_data_location_8]

    data_list_2 = []
           
    data = DataLoader.DataLoader(data_list_2, size_x = res_x,
                                 size_y=res_y, num_inputs=raw_input_size, 
                                 num_outputs=raw_output_size,black_white=True)
    
    

    #data.combine_data(random_sort= True)
    #input_element_1, output_element_1 = data.get_set_elements_to_train(0)

    #pre = net.predict(input_element_1[0])
    #for i in range(0,len(pre)):
        #pred = pre[i]*100
        
       # print("Max Index: "+str(np.argmax(pred))+"  Output: "+str(int(pred[0]))+" "+str(int(pred[1]))+" "+str(int(pred[2]))+" "+str(int(pred[3]))+" "+str(int(pred[4])))
        
    #for i in range(0,15):
        #net.fit(input_element_1[0],output_element_1,verbose = 2)

    socket = EasySocket.EasySocket(preset_unpack_types = ['i'])
    socket.connect()
    while True:
        #get_data = np.array(socket.get_array_data(4,0))
        if(socket.get_anything(4,0) == True): 
            #socket.get_string(1)
            raw_RGB = data.load_image(image_file)
            raw_RGB = np.array(raw_RGB,dtype = np.float32)
            pre = net.predict(np.array([raw_RGB]))
            
            message = str(pre[0][0])+" "+str(pre[0][1])+" "+str(pre[0][2])+" "+str(pre[0][3])+" "+str(pre[0][4])+" "+str(pre[0][5])+" "+str(pre[0][6])+" "+str(pre[0][7])+"\n"

            socket.send_string_data(message)
    socket.close()
