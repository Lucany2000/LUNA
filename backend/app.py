from flask import Flask

app = Flask(__name__)

@app.route('/articles', methods=['GET'])
def get_articles():
    return {"members":["Member1", "member2", "member3"]}


if __name__ == "__main__":
    app.run(debug=True)