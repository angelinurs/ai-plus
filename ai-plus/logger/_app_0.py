from flask import Flask, request
import logging
import logging.handlers

trf_Handler = logging.handlers.TimedRotatingFileHandler(
                                      filename='./logs/app', 
                                      # when='midnight', 
                                      when='M', 
                                      interval=5, 
                                      encoding='utf-8'
                                      )

FILE_HANDLER_FORMAT = '%(asctime)s:[%(levelname)s]:%(name)s:%(threadName)s:%(lineno)s: %(message)s'
formatter = logging.Formatter(FILE_HANDLER_FORMAT)
trf_Handler.setFormatter( formatter )
trf_Handler.suffix = "-%Y%m%d.log"
trf_Handler.doRollover()

logging.getLogger().addHandler(trf_Handler)

# logging.basicConfig(
#                     level=logging.DEBUG, 
#                     format=FILE_HANDLER_FORMAT,
#                     datefmt='%Y-%m-%d %H:%M:%S',
#                     handlers=( 
#                        trf_Handler,
#                        )
#                     )

# Disable flask internal logger.
logging.getLogger('werkzeug').disabled = True

app = Flask( __name__ )

@app.route('/')
def main():
  # showing different logging levels
  app.logger.debug("debug log info")
  app.logger.info("Info log information")
  app.logger.warning("Warning log info")
  app.logger.error("Error log info")
  app.logger.critical("Critical log info")
  return "testing logging levels."

if __name__ == '__main__':
    
    app.run( host='0.0.0.0', debug=False )
