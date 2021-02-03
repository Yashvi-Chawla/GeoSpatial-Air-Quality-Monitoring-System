import firebase_admin
from firebase_admin import credentials
from datetime import datetime,timedelta
cred = credentials.Certificate("./iotssc-1109-firebase-adminsdk-368wj-4e32d1a590.json")
firebase_admin.initialize_app(cred)

from firebase_admin import firestore
database = firestore.client()
def aqi_formula(concentration_low,concentration_high, concentration_present,aqi_low,aqi_high):
    concentration_normalisation = (concentration_present-concentration_low)/(concentration_high-concentration_low)
    aqi_difference = aqi_high-aqi_low
    aqi_final = (concentration_normalisation*aqi_difference)+aqi_low
    return aqi_final
def aqi_nh3(nh3_ug):
    if nh3_ug>=0 and nh3_ug<=200:
        return aqi_formula(0,200,nh3_ug,0,50)
    if nh3_ug>200 and nh3_ug<=400:
        return aqi_formula(200,400,nh3_ug,51,100)
    if nh3_ug>400 and nh3_ug<=800:
        return aqi_formula(400,800,nh3_ug,101,200)
    if nh3_ug>800 and nh3_ug<=1200:
        return aqi_formula(800,1200,nh3_ug,201,300)
    if nh3_ug>1200 and nh3_ug<=1800:
        return aqi_formula(1200,1800,nh3_ug,301,400)
    if nh3_ug>1800:
        if nh3_ug<=2400:
            return aqi_formula(1800,2400,nh3_ug,401,500)
        else:
            return aqi_formula(1800,2400,2400,401,500)
def aqi_co(ug):
    if ug>=0 and ug<=1:
        return aqi_formula(0,1,ug,0,50)
    if ug>1 and ug<=2:
        return aqi_formula(1,2,ug,51,100)
    if ug>2 and ug<=10:
        return aqi_formula(2,10,ug,101,200)
    if ug>10 and ug<=17:
        return aqi_formula(10,17,ug,201,300)
    if ug>17 and ug<=34:
        return aqi_formula(17,34,ug,301,400)
    if ug>34:
        if ug<=68:
            return aqi_formula(34,68,ug,401,500)
        else:
            return aqi_formula(34,68,68,401,500)
def aqi_no2(ug):
    if ug>=0 and ug<=40:
        return aqi_formula(0,40,ug,0,50)
    if ug>40 and ug<=80:
        return aqi_formula(40,80,ug,51,100)
    if ug>80 and ug<=180:
        return aqi_formula(80,180,ug,101,200)
    if ug>180 and ug<=280:
        return aqi_formula(180,280,ug,201,300)
    if ug>280 and ug<=400:
        return aqi_formula(280,400,ug,301,400)
    if ug>400:
        if ug<=520:
            return aqi_formula(400,520,ug,401,500)
        else:
            return aqi_formula(400,520,520,401,500)
def aqi_pm25(ug):
    if ug>=0 and ug<=30:
        return aqi_formula(0,30,ug,0,50)
    if ug>30 and ug<=60:
        return aqi_formula(30,60,ug,51,100)
    if ug>60 and ug<=90:
        return aqi_formula(60,90,ug,101,200)
    if ug>90 and ug<=120:
        return aqi_formula(90,120,ug,201,300)
    if ug>120 and ug<=250:
        return aqi_formula(120,250,ug,301,400)
    if ug>250:
        if ug<=450:
            return aqi_formula(250,450,ug,401,500)
        else:
            return aqi_formula(250,450,450,401,500)
def aqi_average(nh3_ug,co_ug,no2_ug,pm25_ug):
    return (aqi_nh3(nh3_ug)+aqi_co(co_ug)+aqi_no2(no2_ug)+aqi_pm25(pm25_ug))/4

def ug_no2(no2_ppm):
    return (0.0409*no2_ppm*46.01)/1000
def ug_co(co_ppm):
    return (0.0409*co_ppm*28.01)/1000
def ug_nh3(nh3_ppm):
    return (0.0409*nh3_ppm*17.03)/1000
def calculate_aqi(nh3_ppm,co_ppm,no2_ppm,pm25_ug):
    return aqi_average(ug_nh3(nh3_ppm),ug_co(co_ppm),ug_no2(no2_ppm),pm25_ug)
#get all documents from the collectionn
#doc_ref = database.collection(u'sensors_data').stream()
minute_round = 1
hour_round = 1

def twilio(aqi):
    from twilio.rest import Client

    account_sid = "ACe083618ec3fec17ffc4e4db2b1ff76b1"
    # Your Auth Token from twilio.com/console
    auth_token  = "85f171070d97ed109986fb3fb8dc9017"


    status = ""
    client = Client(account_sid, auth_token)
    if aqi >0 and aqi <50:
        status = "air quality is good and posses no harmful impact on your health.\n"
    elif agi>50 and aqi<100:
        status = "moderate"
    message = client.messages.create(
        to="+447566813289",
        from_="+18135139523",
        body=status+" Current Aqi level is :- "+str(aqi))

    print(message.sid)
    print("message sent successfully")

def hourly_collection(collection_type,initial_round):
    from datetime import datetime,timedelta

    date = datetime.today().strftime("%Y.%m.%d")
    if collection_type == "hour_collection":
        present_time = datetime.now()
        past_time = present_time -timedelta(hours=1)
        date = present_time.strftime("%Y.%m.%d")
        present_time = str(present_time.strftime("%H"))+":00:00"
        past_time = str(past_time.strftime("%H"))+":00:00"
    else:
        present_time = datetime.now()
        past_time = present_time -timedelta(minutes=2)
        date = present_time.strftime("%Y.%m.%d")
        present_time = str(present_time.strftime("%H:%M"))+":00"
        past_time = str(past_time.strftime("%H:%M"))+":00"
    #time_2minuteback = datetime.now()-timedelta(minutes=2)
    #present_time = time_2minuteback+timedelta(minutes=2)
    #time_2minuteback = time_2minuteback.strftime("%H.%M")
    #present_time = present_time.strftime("%H.%M")
    if initial_round == 1:
        doc_ref = list(database.collection(u'sensors_data').where(u'date','==',date).where(u"time","<",present_time).stream())
    else:
        doc_ref = (list(database.collection(u'sensors_data').where(u'date','==',date).where(u"time",">=",past_time).where(u"time","<",present_time).stream()))
    prev_time = None
    hourly = False
    minutely = False
    prev_location = None
    for doc in doc_ref:
        elements1 = {}
        elements = doc.to_dict()
        if elements['location'] is None:
            continue
        for key in elements:
            if type(elements[key]) == int or type(elements[key]) == float:
                value = elements[key]
                key = key.strip()
                elements1[key] = value
                continue
            value = elements[key].strip()
            key = key.strip()
            elements1[key] = value
        elements = elements1
        date_time = datetime.strptime(doc.id,"%Y.%m.%d_%H:%M:%S")
        date = date_time.date()
        time = date_time.time()
        print(date,time)
        hour = time.hour
        minute = time.minute
        if len(str(minute)) == 1:
            minute = "0"+str(minute)
        if len(str(hour)) == 1:
            hour = "0"+str(hour)
        if collection_type == "hour_collection":
            hourornimute_collection = database.collection(u'hour_collection').document(str(date)+"_"+str(hour))
            hourorminute_collection1 = hourornimute_collection.get()
            hourly = True
        elif collection_type == "minutely_collection":
            hourornimute_collection = database.collection(u'minutely_collection').document(str(date)+"_"+str(hour)+":"+str(minute))
            hourorminute_collection1 = hourornimute_collection.get()
            minutely = True

        if hourorminute_collection1.to_dict() != None:
            if minutely:
                prev_time = str(date)+"_"+str(hour)+":"+str(minute)
            elif hourly:
                prev_time = str(date)+"_"+str(hour)
            location_collection = database.collection(u'location_'+collection_type).document(elements['location'])
            prev_location = elements['location']
            if location_collection.get().to_dict() == None:
                data = {u'latitude':elements['latitude'],
                        u'longitude':elements['longitude'],
                        u'location':elements['location'],
                        u'peak_AQI':-99,
                        u'NH3':'null',
                        u'CO':'null',
                        u'NO2':'null',
                        u'TVOC':'null',
                        u'eCO2':'null',
                        u'Dust_Concentration':'null',
                        u'time':'null',
                        u'date':'null'}
                location_collection.set(data)
            nh3 = ug_nh3(float(elements['NH3']))
            co = ug_co(float(elements['CO']))
            no2 = ug_no2(float(elements['NO2']))
            dust = float(elements['Dust_Concentration'])
            tvoc = float(elements['TVOC'])
            eco2 = float(elements['eCO2'])
            hourornimute_collection.update({u'NH3': firestore.ArrayUnion([float(elements['NH3'])])})
            hourornimute_collection.update({u'CO': firestore.ArrayUnion([float(elements['CO'])])})
            hourornimute_collection.update({u'NO2': firestore.ArrayUnion([float(elements['NO2'])])})
            hourornimute_collection.update({u'Dust_Concentration': firestore.ArrayUnion([dust])})
            hourornimute_collection.update({u'TVOC': firestore.ArrayUnion([tvoc])})
            hourornimute_collection.update({u'eCO2': firestore.ArrayUnion([eco2])})
            hourornimute_collection.update({u'AQI': firestore.ArrayUnion([calculate_aqi(nh3,co,no2,dust)])})
        else:

            nh3 = ug_nh3(float(elements['NH3']))
            co = ug_co(float(elements['CO']))
            no2 = ug_no2(float(elements['NO2']))
            dust = float(elements['Dust_Concentration'])
            tvoc = float(elements['TVOC'])
            eco2 = float(elements['eCO2'])
            if hourly:
                time_str = float(hour)
            elif minutely:
                if len(str(minute)) == 1:
                    minute = "0"+str(minute)
                else:
                    minute = str(minute)
                time_str = str(hour)+":"+str(minute)
            #print(time_str)
            data = {u'NH3':[float(elements['NH3'])],
                    u'CO':[float(elements['CO'])],
                    u'NO2':[float(elements['NO2'])],
                    u'Dust_Concentration':[dust],
                    u'TVOC':[tvoc],
                    u'eCO2':[eco2],
                    u'AQI':[calculate_aqi(nh3,co,no2,dust)],
                    u'avg_nh3':'null',
                    u'avg_co':'null',
                    u'avg_no2':'null',
                    u'avg_tvoc':'null',
                    u'avg_eco2':'null',
                    u'avg_dust':'null',
                    u'avg_aqi':'null',
                    u'location':elements['location'],
                    u'date':elements['date'],
                    u'time':time_str
                    }
            if minutely:
                database.collection(u'minutely_collection').document(str(date)+"_"+str(hour)+":"+str(minute)).set(data)
            elif hourly:
                database.collection(u'hour_collection').document(str(date)+"_"+str(hour)).set(data)

            #below code invokes when it sees there is change in time either minutely or hourly

            if prev_time != None:
                if minutely:
                    doc_minute = database.collection(u'minutely_collection').document(prev_time)
                elif hourly:
                    doc_minute = database.collection(u'hour_collection').document(prev_time)


                if doc_minute.get().to_dict() != None:

                    elements = doc_minute.get().to_dict()

                    avg_nh3 = sum(elements['NH3'])/len(elements['NH3'])
                    avg_co = sum(elements['CO'])/len(elements['CO'])
                    avg_no2 = sum(elements['NO2'])/len(elements['NO2'])
                    avg_dust = sum(elements['Dust_Concentration'])/len(elements['Dust_Concentration'])
                    avg_aqi = sum(elements['AQI'])/len(elements['AQI'])
                    avg_tvoc = sum(elements['TVOC'])/len(elements['TVOC'])
                    avg_eco2 = sum(elements['eCO2'])/len(elements['eCO2'])
                    data = {'avg_nh3':avg_nh3,'avg_co':avg_co,'avg_no2':avg_no2,'avg_dust':avg_dust,'avg_aqi':avg_aqi,'avg_eco2':avg_eco2,'avg_tvoc':avg_tvoc}
                    time_stamp = elements['time']
                    date_stamp = elements['date']
                    doc_minute.update(data)

                    #this below code invokes to change the peak aqi in location collection
                    if prev_location !=None:
                        location_collection = database.collection(u'location_'+collection_type).document(prev_location)
                        location_elements = location_collection.get().to_dict()
                        if location_elements !=None:
                            prev_aqi = location_elements[u'peak_AQI']
                            print(prev_aqi,avg_aqi)
                            if prev_aqi < avg_aqi:
                                data = {u'peak_AQI':avg_aqi,
                                        u'NH3':avg_nh3,
                                        u'CO':avg_co,
                                        u'NO2':avg_no2,
                                        u'Dust_Concentration':avg_dust,
                                        u'TVOC':avg_tvoc,
                                        u'eCO2':avg_eco2,
                                        u'time':time_stamp,
                                        u'date':date_stamp}
                                location_collection.update(data)
                                if avg_aqi > 50:
                                    twilio(avg_aqi)

    inital = 2
    if "hour_collection":
        import time as tt
        tt.sleep(60)
        hourly_collection("hour_collection",initial_round=inital)
    else:
        import time as tt
        tt.sleep(4)  
        hourly_collection("minutely_collection",initial_round=inital)

hourly_collection("hour_collection",initial_round=hour_round)
#decomment below line when doing minute analysis
#hourly_collection("minutely_collection",initial_round=minute_round)

