import json
import pandas as pd
import numpy as np

from prophet import Prophet

from datetime import datetime
import holidays

from connections.connections import MemDB

import warnings
warnings.filterwarnings('ignore')

# for calculating mse, mae
from sklearn import metrics

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
def run_to_predict(  ts, test_data, period :str, holiday=None ):   
    ts = ts.set_index('ds').resample( period ).mean().reset_index()
    
    m = Prophet(
        
        growth='linear',
    
        ## trend
        # changepoints=None,
        # n_changepoints=25,
        changepoint_range=0.9,    
        changepoint_prior_scale=0.01,
        
        ## seasonality
        # seasonality_mode='multiplicative',
        seasonality_mode='additive', # additive
        seasonality_prior_scale=10.0, # 10
        daily_seasonality=10,
        weekly_seasonality=10,
        # daily_seasonality=True,
        # weekly_seasonality=True,
        # yearly_seasonality=True,
        
        ## holiday
        # holidays=holiday,
        # holidays_prior_scale=10,
        
        ## Others
        interval_width = 0.95,        
        # mcmc_samples = 300,
        # uncertainty_samples=1000,
    )

    m.add_seasonality(name='monthly', period=30.5, fourier_order=5)

    m.fit(ts)
    
    ats = test_data.set_index('ds').resample( period ).mean().reset_index()
    
    ats.dropna(inplace=True)

    forecast = m.predict( ats )
        
    forecast[['ds','yhat','yhat_lower','yhat_upper']].tail()

    result = pd.concat([ats.set_index('ds')['y'], forecast.set_index('ds')[['yhat','yhat_lower','yhat_upper']]], axis=1)

    ## R2-score
    # r2_score = metrics.r2_score(list(result['y']), list(result['yhat']))
    ## MAE(Mean absolute error)
    # mae = metrics.mean_absolute_error(list(result['y']), list(result['yhat']))
    ## MSE(Mean square error)
    # mse = metrics.mean_squared_error(list(result['y']), list(result['yhat']))
    ## RMSE(Root mean square error)
    # rmse = mse**0.5

    result['error'] = result['y'] - result['yhat']
    result['uncertainty'] = result['yhat_upper'] - result['yhat_lower']
    
    result['anomaly'] = result.apply(lambda x: 'Yes' if( np.abs(x['y']) - np.abs(x['yhat']) ) > ( np.abs(x['yhat_upper']) - np.abs(x['yhat_lower']) ) else 'No', axis = 1)
    # result['anomaly'] = result.apply(lambda x: 'Yes' if( np.abs(np.abs(x['y']) - np.abs(x['yhat'])) ) > ( np.abs(np.abs(x['yhat_upper']) - np.abs(x['yhat_lower'])) ) else 'No', axis = 1)
    def custom( hour, y, yhat, yupper, ylower ):
        print( f'index ds : [{hour}]')
        if 8 < hour < 18:
            return 'yes' if y > (yupper + ylower) * 2.0 else 'no'
        else:
            return 'yes' if y > (yupper + ylower)/2 * 0.7 else 'no'
        
    result.reset_index( inplace=True)
    result['anomaly'] = result.apply(lambda x: custom( x['ds'].hour, x['y'], x['yhat'], x['yhat_upper'], x['yhat_lower'] ), axis = 1 )

    ats_data = result.loc[result.anomaly == 'Yes',].reset_index()[['ds', 'y']]
    ats_data.ds = ats_data.ds.apply( lambda x: x.strftime('%Y-%m-%d %H:%M:%S'))

    final = result.reset_index()
    final['days'] = final['ds'].dt.day_name()
    final['hour'] = final.ds.dt.hour
    # final['right'] = final.apply( lambda x: ((x.days in ['Saturday', 'Sunday']) | ((x.hour >= 9) & (x.hour <= 18) ) ) == (x.anomaly == 'Yes'), axis=1)
    final['right'] = final.apply( lambda x: not ((x.days in ['Saturday', 'Sunday']) | ( x.hour not in range( 9, 19 )) ) ^ (x.anomaly == 'Yes'), axis=1)
    # final['is_working_time'] = final.apply( lambda x: not ((x.days in ['Saturday', 'Sunday']) | ( x.hour not in range( 9, 19 )) ), axis=1)
    final = final[['ds', 'days', 'hour', 'y', 'yhat', 'yhat_lower', 'yhat_upper', 'error', 'uncertainty',
        # 'anomaly', 'right', 'is_working_time']]
        'anomaly', 'right']]
    
    accuracy = final.loc[ final.right, ].shape[0] / final.shape[0] * 100
    # accuracy_working_time = final.loc[ final.is_working_time ].shape[0] / final.shape[0] * 100
    # accuracy_not_working_time = final.loc[ final.right, ].shape[0] / final.shape[0] * 100

    # Accuracy
    print('Accuracy: {0}'.format(accuracy))
    f = open('./accuracy.csv', 'a')
    f.write('ds,y,yhat,yhat_upper,yhat_lower,hour,days,anomaly,right\n')
    for i in range(0, len(final)):
        f.write('{0},{1},{2},{3},{4},{5},{6},{7},{8}\n'
                .format(final['ds'][i],final['y'][i],final['yhat'][i],
                        final['yhat_upper'][i],final['yhat_lower'][i],
                        final['hour'][i],final['days'][i],final['anomaly'][i],final['right'][i]))
    f.write('Accuracy: {0}\n'.format(accuracy))
    f.close()

    with open('./only_accuracy.csv', 'a') as acc_f:
        acc_f.write('Accuracy: {0}\n'.format(accuracy))

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
        holiday = get_holidays( '2019-01-01', '2023-12-31' )
    
    result_dict =  run_to_predict(  ts, test_df, '30T' )
    
    return {
        'name'   : jsondata.get('name'),
        'seq'    : f'seq:{seq_no}',
        'status' : 1,
        'anomal' : list(result_dict.get('result').transpose().to_dict().values())
    }
    
# main Get anomal dictionary data
def req_predict( seq_no, data, start = None, end = None ):
    jsondata = json.loads( data )
    
    test_df = pd.DataFrame( data=jsondata.get( 'today_logs' ),  )
    test_df['ds'] = test_df['ds'].apply( lambda x: datetime.strptime(x, '%Y%m%d-%H%M'))
    
    ts = pd.DataFrame( data=jsondata.get( 'logs' ),  )
    ts['ds'] = ts['ds'].apply( lambda x: datetime.strptime(x, '%Y%m%d-%H%M'))
    
    if( start is not None and end is not None ):
        holiday = get_holidays( '2019-01-01', '2023-12-31' )
    
    result_dict =  run_to_predict(  ts, test_df, '1T' )
    anomal_list = list(result_dict.get('result').transpose().to_dict().values())
    
    memDB = MemDB()
    memDB.consume_data( seq_no, anomal_list  )
