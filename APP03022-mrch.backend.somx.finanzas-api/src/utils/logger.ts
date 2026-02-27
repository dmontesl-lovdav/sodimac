import winston from "winston";

export const logger = winston.createLogger({
  level: "info",
  format: winston.format.combine(
    winston.format.colorize(),
    winston.format.timestamp(),
    winston.format.printf(
      ({ level, timestamp, message }) => `${level}: ${timestamp} : ${message}`
    )
  ),
  transports: [new winston.transports.Console()],
});