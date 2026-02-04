from flask import Flask, Response
import os
from email.utils import formatdate as email_formatdate

import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("mock_host.serve_compressed_rdata")

app = Flask(__name__)


@app.route("/", defaults={"path": ""})
@app.route("/<path:path>")
def serve_matchmaker_data(path):
    file_path = "./data.txt.gz"

    st = os.stat(file_path)

    headers = {
        "Cache-Control": "no-cache",
        "Last-Modified": email_formatdate(st.st_mtime, usegmt=True),
        "Content-Type": "text/plain; charset=utf-8",
        "Content-Encoding": "gzip",
        "Content-Length": "%d" % st.st_size,
    }

    logger.info(f"Serving file with headers = {headers}")

    with open(file_path, 'rb') as f:
        data = f.read()

    return Response(data, headers=headers,)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8889)
