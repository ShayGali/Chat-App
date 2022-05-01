const SocketServer = require("websocket").server;
const http = require("http");

const server = http.createServer((req, res) => {});

const PORT = 3000;

server.listen(PORT, () => {
  console.log(`Listening on port ${PORT}...`);
});

webSocketServer = new SocketServer({ httpServer: server });

let connections = [];

webSocketServer.on("request", (req) => {
  const connection = req.accept();

  console.log(`New connection`);
  connections.push(connection);

  connection.on("message", (msg) => {
    console.log(msg);
    connections.forEach((element) => {
      if (element !== connection) element.sendUTF(msg.utf8Data);
    });
  });

  connection.on("close", (resCode, desc) => {
    console.log("connection closed");
    connections = connections.filter((element) => element !== connection);
  });
});
