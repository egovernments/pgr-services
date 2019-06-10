import http from "http";
import express from "express";
import cors from "cors";
import morgan from "morgan";
import config from "./config.json";
import swaggerTools from "swagger-tools";
import bodyParser from "body-parser";
import api from "./controller";
const { Pool } = require("pg");
import tracer from "./middleware/tracer";

//loading env property
require("dotenv").config();

const pool = new Pool({
  user: process.env.DB_USER,
  host: process.env.DB_HOST,
  database: process.env.DB_NAME,
  password: process.env.DB_PASSWORD,
  port: process.env.DB_PORT
});

const options = {
  controllers: "./src/api",
  useStubs: true // Conditionally turn on stubs (mock mode)
};

let app = express();
app.server = http.createServer(app);
app.use(bodyParser.json());

// logger
app.use(morgan("dev"));

// 3rd party middleware
app.use(
  cors({
    exposedHeaders: config.corsHeaders
  })
);

app.use(tracer());

let swaggerDoc = require("../config/docs/contract/swagger.json");

swaggerTools.initializeMiddleware(swaggerDoc, middleware => {
  app.use(middleware.swaggerMetadata());

  // Validate Swagger requests
  app.use(middleware.swaggerValidator());

  // Route validated requests to appropriate controller
  // app.use(middleware.swaggerRouter(options));

  // Serve the Swagger documents and Swagger UI
  app.use(middleware.swaggerUi());
  let serverPort = process.env.SERVER_PORT;
  app.server.listen(serverPort, () => {
    console.log("port is ", serverPort);
  });
});
app.use("/", api(pool));
export default app;
