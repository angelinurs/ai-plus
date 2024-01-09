import json
import pandas as pd
import numpy as np

from prophet import Prophet

from datetime import datetime, timedelta
import holidays

from connections.connections import MemDB

import warnings
warnings.filterwarnings('ignore')

from flask import Flask

app = Flask(__name__)

'''
function : 
    - get_holidays( start, end )
    - run_to_predict(  ts :Dataframe, test_data :Dataframe, period :str, holiday:Dataframe )
    - get_anomal( data ) 
'''
#-------------------------

# Get Holidays  - as seasonality
# can skip follow function
def get_holidays( start, end ):

    app.logger.info("Excute method reqget_holidays")
    
    series = pd.date_range( start=start, end=end, freq='D' )

    kr_holidays = holidays.KR()

    holiday_df = pd.DataFrame( columns=['ds','holiday'])
    holiday_df['ds'] = series

    holiday_df['holiday'] = holiday_df.ds.apply( lambda x: '1' if x in kr_holidays else '0' )

    holiday = holiday_df.loc[ holiday_df.holiday == '1', ].reset_index()[['holiday', 'ds']]
    holiday['holiday'] = holiday.holiday.apply( lambda x: 'holiday' if x == '1' else x )
    return holiday
# ------------------

# Get Holidays  - as seasonality
def run_to_predict(  train, test, period :str, holiday=None ):  
     
    app.logger.info("Excute method run_to_predict")

    m = Prophet(
            
            growth='linear',
            # growth='logistic',
            # growth='flat',
        
            ## trend
            # changepoints=None,
            # n_changepoints=25,
            changepoint_range=0.9,    
            changepoint_prior_scale=0.1,
            
            # ## seasonality
            seasonality_mode='multiplicative',
            # seasonality_mode='additive', # additive
            seasonality_prior_scale=10.0, # 10
            daily_seasonality=10.0,
            weekly_seasonality=0.05,
            # daily_seasonality=False,
            # weekly_seasonality=False,
            # yearly_seasonality=False,
            
            ## holiday
            # holidays=holiday,
            holidays_prior_scale=10,
            
            ## Others
            # interval_width = 0.95,        
            # interval_width = 0.99,        
            # mcmc_samples = 300,
            # uncertainty_samples=1000,
        
    )

    ######################

    tr = train.set_index('ds').resample( period ).mean().reset_index()
    tr.y.fillna(0, inplace=True)
    tr["is_weekend"] = tr["ds"].apply( lambda x: 1 if x.weekday in (5, 6) else 0 )
    m.add_seasonality(name='weekend_daily', period=1, fourier_order=5, condition_name='is_weekend', prior_scale=10 )
    m.fit(tr)

    test["is_weekend"] = test["ds"].apply( lambda x: 1 if x.weekday in (5, 6) else 0 )

    pred = m.predict( test[['ds', 'y', 'is_weekend']] )
    
    pred_dday = pd.concat([test.set_index('ds')['y'], pred.set_index('ds')[['yhat','yhat_lower','yhat_upper']]], axis=1)
    pred_dday.reset_index( inplace=True )

    ######################
    # pred_dday['pow_yhat_up'] = np.power( pred_dday['yhat_upper'], 4 ) * 1.5
    pred_dday['yhat_up_mul'] = pred_dday['yhat_upper'] * 1.68
    # pred_dday['pow_yhat_up_yhat_lower'] = pred_dday.pow_yhat_up - np.abs(pred_dday.yhat_lower)

    anomal = pred_dday.copy()
    # anomal['anomaly'] = anomal.apply(lambda x: 1 if  ( x.y - x.pow_yhat_up + np.abs(x.yhat_lower)  ) > 0  else 0, axis = 1)
    # anomal['anomaly'] = anomal.apply(lambda x: 1 if  ( x.y - x.yhat_up_mul ) > 0  else 0, axis = 1)
    anomal['anomaly'] = anomal.apply(lambda x: 1 if x.y > x.yhat * 5 else 0, axis = 1)
    
    anomal[anomal.anomaly == 1]

    ######################
    
    # ats_data.ds = ats_data.ds.apply( lambda x: x.strftime('%Y-%m-%d %H:%M:%S'))
    # ats_data = anomal[anomal.anomaly == 1].reset_index()
    # ats_data.ds.apply( lambda x: x.strftime('%Y-%m-%d %H:%M:%S')).values
    ats_data = anomal[anomal.anomaly == 1].ds.apply( lambda x: x.strftime('%Y-%m-%d %H:%M:%S')).values
    
    return {
                'result'   : ats_data,
            }
# -------------------

# main Get anomal dictionary data
def get_anomal( seq_no, data, start = None, end = None ):
    
    jsondata = json.loads( data )
    
    test_df = pd.DataFrame( data=jsondata.get( 'today_logs' ),  )
    test_df['ds'] = test_df['ds'].apply( lambda x: datetime.strptime(x, '%Y%m%d-%H%M'))
    
    ts = pd.DataFrame( data=jsondata.get( 'logs' ),  )
    ts['ds'] = ts['ds'].apply( lambda x: datetime.strptime(x, '%Y%m%d-%H%M'))
    
    if( start is not None and end is not None ):
        days = datetime.now().strftime('%Y-%m-%d'), (datetime.now() - timedelta(days=90)).strftime('%Y-%m-%d')        
        holiday = get_holidays( *days[::-1] )
    
    result_dict =  run_to_predict(  ts, test_df, '1T' )
    
    return {
        'name'   : jsondata.get('name'),
        'seq'    : f'seq:{seq_no}',
        'status' : 1,
        'anomal' : list(result_dict.get('result'))
    }
    
# main Get anomal dictionary data
def req_predict( seq_no, data, start = None, end = None ):
    app.logger.info("Excute method req_predict")
    jsondata = json.loads( data )
    
    test_df = pd.DataFrame( data=jsondata.get( 'today_logs' ),  )
    test_df['ds'] = test_df['ds'].apply( lambda x: datetime.strptime(x, '%Y%m%d-%H%M'))
    
    ts = pd.DataFrame( data=jsondata.get( 'logs' ),  )
    ts['ds'] = ts['ds'].apply( lambda x: datetime.strptime(x, '%Y%m%d-%H%M'))
    
    if( start is not None and end is not None ):
        days = datetime.now().strftime('%Y-%m-%d'), (datetime.now() - timedelta(days=90)).strftime('%Y-%m-%d')        
        holiday = get_holidays( *days[::-1] )
    
    result_dict =  run_to_predict(  ts, test_df, '1T' )
    anomal_list = list(result_dict.get('result'))
    
    print( anomal_list )
    
    memDB = MemDB()
    memDB.consume_data( seq_no, anomal_list  )
