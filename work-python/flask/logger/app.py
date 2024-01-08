from flask import Flask, request
import logging
import logging.handlers

logging.basicConfig(
                    level=logging.DEBUG, 
                    format='%(asctime)s:[%(levelname)s]:%(name)s:%(threadName)s:%(lineno)s: %(message)s',
                    datefmt='%Y-%m-%d %I:%M:%S%p',
                    handlers=( logging.handlers.RotatingFileHandler(filename='./logs/app.log', mode='a', maxBytes=1024 * 1024 * 100, backupCount=10, encoding=None, delay=False, errors=None), 
                               logging.StreamHandler())
                    )

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
