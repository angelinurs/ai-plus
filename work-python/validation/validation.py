import pandas as pd
import numpy as np

from prophet import Prophet
from prophet.plot import add_changepoints_to_plot
from prophet.diagnostics import cross_validation, performance_metrics

from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error

import plotly.express as px

import pickle
import itertools

from dateutil.parser import *

import warnings
warnings.filterwarnings('ignore')
    

import holidays

# Get holidays
def get_holidays( start, end ):

    series = pd.date_range( start=start, end=end, freq='D' )

    kr_holidays = holidays.KR()

    holiday_df = pd.DataFrame( columns=['ds','holiday'])
    holiday_df['ds'] = series

    holiday_df['holiday'] = holiday_df.ds.apply( lambda x: '1' if x in kr_holidays else '0' )

    holiday = holiday_df.loc[ holiday_df.holiday == '1', ].reset_index()[['holiday', 'ds']]
    holiday['holiday'] = holiday.holiday.apply( lambda x: 'holiday' if x == '1' else x )
    return holiday

# 1 분 단위로 count 된 log 
filename = '../ai-plus-test-data/logs_yjan.csv'
df = pd.read_csv( filename, sep=',', engine='python')
df.columns = [ 'ds', 'y' ]

df.ds = pd.to_datetime(df['ds'], format='%Y%m%d-%H%M')

with open( '../ai-plus-test-data/test_data.pickle', 'rb') as fr:
    df_test = pickle.load( fr )

# find best model

holiday_df = get_holidays( '2023-02-01', '2023-05-02' )

search_space = {
 'changepoint_prior_scale': [0.05, 0.1, 0.5, 1.0, 5.0, 10.0],
 'seasonality_prior_scale': [0.05, 0.1, 1.0, 10.0],
 'holidays_prior_scale': [0.05, 0.1, 1.0, 10.0],
#  'seasonality_mode': ['additive', 'multiplicative'],
 'seasonality_mode': ['additive', ],
 'holidays': [holiday_df]
}

param_combined = [dict(zip(search_space.keys(), v)) for v in itertools.product(*search_space.values())]

mapes = []
for param in param_combined:
   print('params', param)
   _m = Prophet(**param)
  #  if regressors is not None:
  #    for regressor in regressors:
  #      _m.add_regressor(regressor)

   _m.fit(df)
   _cv_df = cross_validation(_m, horizon='24 hours', parallel='threads')
   _df_p = performance_metrics(_cv_df, rolling_window=1)
   mapes.append(_df_p['mape'].values[0])

tuning_results = pd.DataFrame(param_combined)
tuning_results['mapes'] = mapes


filename = '../ai-plus-test-data/validation.pickle'
with open( filename, 'wb') as f:
        pickle.dump(tuning_results, f)