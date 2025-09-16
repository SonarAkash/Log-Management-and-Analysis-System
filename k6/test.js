import http from 'k6/http';
import { check } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

const API_TARGET = __ENV.API_TARGET || 'http://localhost:8080';
const API_TOKEN = __ENV.API_TOKEN || 'w7CY_y-lt0ja3HskjK2TSMdJ_ybMmg9oVDe7lOvX4no';
const INGEST_URL = `${API_TARGET}/api/v1/ingest`;

const SERVICES = ["payment-service", "user-service", "order-service"];
const INFO_MESSAGES = [
    "User logged in successfully",
    "Payment processed for order",
    "Item added to cart",
    "User profile updated"
];
const ERROR_MESSAGES = [
    "Failed to connect to database",
    "API rate limit exceeded",
    "Null pointer exception while processing request"
];

export const options = {
  stages: [
    { duration: '30s', target: 50 },    // Ramp up to 50 concurrent users
    { duration: '1m', target: 200 },   // Spike to 200 concurrent users
    { duration: '30s', target: 0 },     // Ramp down
  ],
};

// Main function that each virtual user runs repeatedly
export default function () {
  let payload;
  let contentType;

  const choice = Math.floor(Math.random() * 3);
  switch (choice) {
    case 0: // JSON
      contentType = 'application/json';
      payload = createJsonPayload();
      break;
    case 1: // logfmt
      contentType = 'text/plain';
      payload = createLogfmtPayload();
      break;
    default: // Unstructured
      contentType = 'text/plain';
      payload = createUnstructuredPayload();
      break;
  }

  const params = {
    headers: {
      'Content-Type': contentType,
      'X-Tenant-Api-Key': API_TOKEN,
    },
  };

  const res = http.post(INGEST_URL, payload, params);

  check(res, {
    'is status 202 (Accepted) or 429 (Too Many Requests)': (r) => r.status === 202 || r.status === 429,
  });
}

// --- Payload Generator Functions ---

function createJsonPayload() {
  const data = {
    timestamp: new Date().toISOString(),
    level: "INFO",
    message: INFO_MESSAGES[Math.floor(Math.random() * INFO_MESSAGES.length)],
    service: SERVICES[Math.floor(Math.random() * SERVICES.length)],
    details: { trace_id: `trace-${__VU}-${__ITER}` }, // __VU and __ITER are k6 variables
  };
  return JSON.stringify(data);
}

function createLogfmtPayload() {
  const service = SERVICES[Math.floor(Math.random() * SERVICES.length)];
  const message = INFO_MESSAGES[Math.floor(Math.random() * INFO_MESSAGES.length)];
  return `ts=${new Date().toISOString()} level=WARN service=${service} msg="${message}" request_id=req-${__VU}-${__ITER}`;
}

function createUnstructuredPayload() {
  const errorMessage = ERROR_MESSAGES[Math.floor(Math.random() * ERROR_MESSAGES.length)];
  return `${new Date().toISOString()} [worker-thread-${__VU}] ERROR com.logmanager.Processor - ${errorMessage}\n\tat com.logmanager.Service.handle(Service.java:123)`;
}