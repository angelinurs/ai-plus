from flask import Flask, request
from urllib import parse

# forecast model
from module.forecast import get_anomal, req_predict
# manipulation class for redis
from connections.connections import MemDB

# from multiprocessing import Process
from multiprocessing.dummy import Pool
from multiprocessing import cpu_count

import _thread

import json

# logger
import logging
import logging.handlers

# daily log rotation
trf_Handler = logging.handlers.TimedRotatingFileHandler(
                                      filename='./logs/app', 
                                      when='midnight', 
                                      interval=1, 
                                      encoding='utf-8',
                                      backupCount=5
                                      )
trf_Handler.suffix = "-%Y%m%d.log"

logging.basicConfig(
                    level=logging.DEBUG, 
                    format='%(asctime)s:[%(levelname)s]:%(name)s:%(threadName)s:%(lineno)s: %(message)s',
                    datefmt='%Y-%m-%d %I:%M:%S%p',
                    handlers=( 
                        trf_Handler,
                        # 100 mb file rotate max 10 count
                        # logging.handlers.RotatingFileHandler(filename='./logs/app.log', mode='a', maxBytes=1024 * 1024 * 100, backupCount=10, encoding=None, delay=False, errors=None), 
                        # logging.StreamHandler()
                        ),
                    
                    )

# Disable flask internal logger.
logging.getLogger('werkzeug').disabled = True

app = Flask( __name__ )
# redis custom connection and manupulator
memDB = MemDB()

# verion 0 
@app.route( '/predict', methods=['POST'] )
def test_post():
    if request.method == 'POST':
        payload = request.get_data(cache=True, as_text=True, parse_form_data=False)
                
        payload = parse.unquote(payload)
        # app.logger.info( payload[3:] )
        
        payload = get_anomal( payload[3:] )
        
        # app.logger.info( payload )
            
        return payload
    else:
        return f'not post request'
    
    return payload['message'], 200

# request predict
@app.route( '/req_predict', methods=['POST'] )
def req_predict_post():
    if request.method == 'POST':
	
        ip  = request.environ['REMOTE_ADDR'] if request.environ.get('HTTP_X_FORWARDED_FOR') is None else request.environ['HTTP_X_FORWARDED_FOR']
        # app.logger.info( f'{ip} request')

        payload = request.get_data(cache=True, as_text=True, parse_form_data=False)
                
        payload = parse.unquote(payload)
        # app.logger.info( payload[3:] )

        seq_no, data = memDB.init_data()
        
        app.logger.info( f'{ip} request -> seq_no [{seq_no}]' )
        
        # pool = Pool(cpu_count())
        # pool.apply_async( req_predict( seq_no, payload[3:] ) )
        # pool.close()
        _thread.start_new_thread(req_predict,( seq_no, payload[3:],))
        
        # app.logger.info( data )
            
        return data
    else:
        return f'not post request'
    
    return data['message'], 200

# polling predict
@app.route( '/polling_predict', methods=['POST'] )
def polling_predict_post():
    if request.method == 'POST':

        ip  = request.environ['REMOTE_ADDR'] if request.environ.get('HTTP_X_FORWARDED_FOR') is None else request.environ['HTTP_X_FORWARDED_FOR']
        # app.logger.info( f'{ip} polling')

        payload = request.get_data(cache=True, as_text=True, parse_form_data=False)
                
        payload = parse.unquote(payload)
        
        seq_no = json.loads(payload[3:]).get('seq')
        app.logger.info( f'{ip} polling -> seq_no : {seq_no}' )
        
        data = memDB.get_data( str(seq_no) )
        app.logger.info( f'data : {data}' )
        
        # # payload = payload.decode('UTF-8')
        # payload = json.loads(payload[3:])
            
        return data
    else:
        return f'not post request'
    
    return data['message'], 200
    
if __name__ == '__main__':

    app.run(debug=False, host="0.0.0.0", port="5000")

# Run with Gunicorn
# gunicorn app:app -b 0.0.0.0:5000 --workers 2 -k gevent --timeout 300 --worker-connections 1000 --max-requests 1000000 --limit-request-line 8190 --access-logfile '-' --error-logfile '-'