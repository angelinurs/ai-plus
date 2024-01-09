import redis
import datetime
import json

from flask import Flask

app = Flask(__name__)

class MemDB:
    def __init__(self ):        
        self.pool = redis.ConnectionPool( 
                                host='localhost', 
                                port=6379,
                                db=0
                                )
        self.r = redis.Redis( connection_pool=self.pool )
        
    # insert: initialize data 
    def init_data( self ):
        
        # use pipeline because of concurrency
        # req_seq is like a primary key 
        pipe = self.r.pipeline( transaction=False )
        pipe.get( 'req_seq' )
        pipe.incr( 'req_seq' )
        result = pipe.execute()
        seq_no = str( result[1] )
        
        # req_date format : '20230525 18:15:06' 
        req_date = datetime.datetime.now().strftime('%Y%m%d %H:%M:%S')       

        data =  {
                'seq': seq_no,
                'req-date': req_date,
                #`'res-date': '',
                'status-code': '0',
                # 'anomal-data': dict()
        }

        app.logger.info( data )

        temp = json.dumps( data )
        seq_no = f'seq:{str(seq_no)}'
        
        self.r.set( seq_no, temp )
        
        return seq_no, data
        
    # insert : after response data 
    def consume_data( self, seq_no: str, anomal_list: list  ):
        
       # req_date format : '20230525 18:15:06' 
       completion_date = datetime.datetime.now().strftime('%Y%m%d %H:%M:%S')
       
       # data load to redis
       data = json.loads( self.r.get( seq_no ) )
       
       # data update
       data.update({
            'completion-date': completion_date,
            'status-code' : '1',
            'anomal' : anomal_list
       })

       print( data )
       
       # data save to redis
       temp = json.dumps( data )

       app.logger.info( data )
       
       self.r.set( seq_no, temp )
       
    # polling : anomal result data   
    def get_data( self, seq_no: str ):
           
       # data load to redis
       data = json.loads( str(self.r.get( f'seq:{seq_no}' )) )

       app.logger.info( data )
       
       return data
        
    
   
    
