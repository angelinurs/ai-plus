from flask import Flask
from flask_restx import Api, Resource

app = Flask( __name__ )
api = Api( app )

@app.route('/')
def func():
    return 'test app'

@app.route('/<user>')
def by_user( user ):
    return f'user : {user}'

@app.route('/user_id/<int:user_id>')
def by_user_id( user_id ):
    return f'user_id : {user_id}'

@api.route('/api')
class GetApi( Resource ):
    def get( self):
        return {'username':'naru'}

@api.route('/api/<string:name>')
class IsAnomal( Resource ):
    def get( self):
        return {'username':'naru'}
    
@api.route('/api/json', method=['POST'])
class IsAnomal( Resource ):
    def get( self):
        return {'username':'naru'}
    

# @app.route('/info', method=[ 'POST',] )
# def by_post(  ):
#     if request.method == 'POST':
#         return f'post request ok'
#     else:
#         return f'not post request'

if __name__ == '__main__':    
    app.run( host='0.0.0.0', debug=True )
