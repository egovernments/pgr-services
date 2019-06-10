import { version } from "../../package.json";
import { Router } from "express";
import create from "./create";
import search from "./search";
import update from "./update";

export default ({ config, db }) => {
  let api = Router();

  api.use("/firenoc-services/v1", create({ config, db }));
  api.use("/firenoc-services/v1", search({ config, db }));
  api.use("/firenoc-services/v1", update({ config, db }));
  // perhaps expose some API metadata at the root
  api.get("/", (req, res) => {
    res.json({ version });
  });

  return api;
};
