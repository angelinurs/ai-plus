import json
import requests
import logging
import os
from logging.config import dictConfig

from module.occurlog import makelogs

# debug settings
debug = eval(os.environ.get("DEBUG", "False"))

from flask import Flask, make_response, request

# for sending error logs to slack
# class HTTPSlackHandler(logging.Handler):
#     def emit(self, record):
#         log_entry = self.format(record)
#         json_text = json.dumps({"text": log_entry})
#         url = 'https://hooks.slack.com/services/<org_id>/<api_key>'
#         return requests.post(url, json_text, headers={"Content-type": "application/json"}).content


dictConfig({
    "version": 1,
    "disable_existing_loggers": True,
    "formatters": {
        "default": {
            "format": "[%(asctime)s] %(levelname)s in %(module)s: %(message)s",
        },
        "access": {
            "format": "%(message)s",
        }
    },
    "handlers": {
        "console": {
            "level": "INFO",
            "class": "logging.StreamHandler",
            "formatter": "default",
            "stream": "ext://sys.stdout",
        },
        # "email": {
        #     "class": "logging.handlers.SMTPHandler",
        #     "formatter": "default",
        #     "level": "ERROR",
        #     "mailhost": ("smtp.example.com", 587),
        #     "fromaddr": "devops@example.com",
        #     "toaddrs": ["receiver@example.com", "receiver2@example.com"],
        #     "subject": "Error Logs",
        #     "credentials": ("username", "password"),
        # },
        # "slack": {
        #     "class": "app.HTTPSlackHandler",
        #     "formatter": "default",
        #     "level": "ERROR",
        # },
        "error_file": {
            "class": "logging.handlers.RotatingFileHandler",
            "formatter": "default",
            # "filename": "/var/log/gunicorn.error.log",
            "filename": "./logs/gunicorn.error.log",
            "maxBytes": 10000,
            "backupCount": 10,
            "delay": "True",
        },
        "access_file": {
            "class": "logging.handlers.RotatingFileHandler",
            "formatter": "access",
            # "filename": "/var/log/gunicorn.access.log",
            "filename": "./logs/gunicorn.access.log",
            "maxBytes": 10000,
            "backupCount": 10,
            "delay": "True",
        }
    },
    "loggers": {
        "gunicorn.error": {
            # "handlers": ["console"] if debug else ["console", "slack", "error_file"],
            "handlers": ["console"] if debug else ["console", "error_file"],
            "level": "ERROR",
            "propagate": False,
        },
        "gunicorn.access": {
            "handlers": ["console"] if debug else ["console", "access_file"],
            "level": "INFO",
            "propagate": False,
        }
    },
    "root": {
        "level": "DEBUG" if debug else "INFO",
        # "handlers": ["console"] if debug else ["console", "slack"],
        "handlers": ["console"] if debug else ["console",],
    }
})


app = Flask(__name__)

@app.route("/status", methods=["GET"])
def health_check():
    logging.debug("debug log")
    logging.info("info log")
    logging.warning("warning log")
    logging.error("error log")
    # logging.exception("exception log")

    makelogs()
    
    return make_response("OK", 200)


if __name__ == "__main__":
    app.run(debug=debug, host="0.0.0.0", port="5000")

# Run with Gunicorn
# gunicorn app:app -b 0.0.0.0:5000 --workers 2 -k gevent --timeout 300 --worker-connections 1000 --max-requests 1000000 --limit-request-line 8190 --access-logfile '-' --error-logfile '-'