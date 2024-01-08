#### logger_mod.py
import os
import logging

from logging.config import dictConfig
from app import app

class CustomFilter(logging.Filter):

    def filter(self, record):
        record.appName = 'my-application-name'
        return True

logging_configuration = {
    'version': 1,
    "disable_existing_loggers": False,
    'formatters': {
        'default': {
            'format': '[%(asctime)s] %(levelname)s - APP:%(appName)s - MODULE:%(module)s - FUNCTION:%(funcName)s - LINE:%(lineno)d : %(message)s',
        }
    },
    'filters': {
        'additional': {
            '()': CustomFilter
        }
    },
    'handlers': {
        'console': {
            'class': 'logging.StreamHandler',
            'formatter': 'default',
            'level': 'INFO',
            'stream': 'ext://sys.stdout'
        },
        "info_file": {
            "class": "logging.handlers.RotatingFileHandler",
            "formatter": "default",
            "filename": app.config['APPLICATION_LOG_PATH'] + "/info.log",
            "maxBytes": 10000,
            "backupCount": 10,
            "delay": "True",
            'level': 'INFO',
        },
        "error_file": {
            "class": "logging.handlers.RotatingFileHandler",
            "formatter": "default",
            "filename": app.config['APPLICATION_LOG_PATH'] + "/error.log",
            "maxBytes": 10000,
            "backupCount": 10,
            "delay": "True",
            'level': 'ERROR',
        }
    },
    'loggers': {
        'testProject': {
            'handlers': ['console',  'info_file', 'error_file'],
            'level': 'INFO',
            'filters': ['additional'],
            'propagate': False
        }
    }
}

def setup_logging():
    # Create logs folder if does not exist.
    if not os.path.exists(app.config['APPLICATION_LOG_PATH']):
        os.makedirs(app.config['APPLICATION_LOG_PATH'])

    dictConfig(logging_configuration)

    # Disable flask internal logger.
    logging.getLogger('werkzeug').disabled = True
