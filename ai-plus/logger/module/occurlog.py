from flask import Flask

# logger = logging.getLogger('__main__')
app = Flask(__name__)


def makelogs():
    app.logger.info("occurlogs < INFO >")
    app.logger.error("occurlogs < ERROR >")
