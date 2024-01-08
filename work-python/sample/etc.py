import logging
from flask import Flask

logger = logging.getLogger('ETC_notebook')

app = Flask( __name__ )

# Define a function here.
def temp_convert(var):
   try:
      return int(var)
   except Exception as e:
      logger.error( e.args[0] )
    #   print( "The argument does not contain numbers\n", e.args[0] )

if __name__ == '__main__':
    setup_logging()
    
    app.run( host='0.0.0.0', debug=False )

    # Call above function here.
    temp_convert("xyz")