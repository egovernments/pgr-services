import logger from "../config/logger";

module.exports = function(options) {
  return function(req, res, next) {
    const TRACER_ENABLE_REQUEST_LOGGING =
      process.env.TRACER_ENABLE_REQUEST_LOGGING === "true";

    logger.info(`Received request URI: ${req.originalUrl}`);

    if (
      TRACER_ENABLE_REQUEST_LOGGING &&
      req.method === "POST" &&
      req.is("application/json")
        ? true
        : false
    ) {
      logger.info(`Request body - ${JSON.stringify(req.body)}`);
      logger.info(`Request body - ${JSON.stringify(req.query)}`);
    }

    res.on("finish", function() {
      logger.info(`Response code sent: ${this.statusCode}`);
    });

    next();
  };
};
