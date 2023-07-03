from flask import Flask, jsonify

app = Flask(__name__)

@app.route('/articles', methods=['GET'])
def get_articles():
    return {"members":["member1", "member2", "member3"]}


if __name__ == "__main__":
    app.run(debug=True)