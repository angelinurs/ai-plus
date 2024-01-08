import logging
import os

from module.occurlog import makelogs

from flask import Flask, make_response, request

# log set up 
# - begin
import logging.config, os, yaml

loggingConfigPath = './config/logging.yaml'
if os.path.exists(loggingConfigPath):
    with open(loggingConfigPath, 'rt') as f:
        loggingConfig = yaml.full_load(f.read())
        logging.config.dictConfig(loggingConfig)
else:
    logging.basicConfig(level=logging.INFO)

# Disable flask internal logger.
logging.getLogger('werkzeug').disabled = True
# log set up 
# - end


app = Flask(__name__)
logger = logging.getLogger(__name__)

@app.route("/status", methods=["GET"])
def health_check():
    app.logger.debug("debug log")
    app.logger.info("info log")
    app.logger.warning("warning log")
    app.logger.error("error log")
    logging.info("merong")

    # logging.exception("exception log")

    makelogs()
    
    return make_response("OK", 200)


if __name__ == "__main__":
    app.run(debug=False, host="0.0.0.0", port="5000")

# Run with Gunicorn
# gunicorn app:app -b 0.0.0.0:5000 --workers 2 -k gevent --timeout 300 --worker-connections 1000 --max-requests 1000000 --limit-request-line 8190 --access-logfile '-' --error-logfile '-'