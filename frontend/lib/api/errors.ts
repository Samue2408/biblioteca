export type ApiErrorCode =
  | "EMAIL_ALREADY_IN_USE"
  | "BAD_CREDENTIALS"
  | "NOT_FOUND"
  | "BOOK_NOT_AVAILABLE"
  | "USER_BLOCKED"
  | "LOAN_ALREADY_RETURNED"
  | "DUPLICATE_RESERVATION"
  | "EXTERNAL_LOOKUP_FAILED"
  | "MISSING_BOOK_DATA"
  | "DUPLICATE_ISBN"
  | "BOOK_NOT_DELETABLE"
  | "BOOK_NOT_RESTORABLE"
  | "NETWORK_ERROR"
  | "UNKNOWN";

export type ApiErrorBody = {
  timestamp?: string;
  status: number;
  code: string;
  message: string;
  path?: string;
  reservationsAhead?: number;
};

export class ApiException extends Error {
  readonly status: number;
  readonly code: ApiErrorCode;
  readonly path?: string;
  readonly reservationsAhead?: number;

  constructor(body: {
    status: number;
    code: ApiErrorCode;
    message: string;
    path?: string;
    reservationsAhead?: number;
  }) {
    super(body.message);
    this.name = "ApiException";
    this.status = body.status;
    this.code = body.code;
    this.path = body.path;
    this.reservationsAhead = body.reservationsAhead;
  }
}

function isApiErrorBody(value: unknown): value is ApiErrorBody {
  if (typeof value !== "object" || value === null) {
    return false;
  }
  const record = value as Record<string, unknown>;
  return (
    typeof record.status === "number" &&
    typeof record.code === "string" &&
    typeof record.message === "string"
  );
}

const KNOWN_CODES: ReadonlySet<string> = new Set([
  "EMAIL_ALREADY_IN_USE",
  "BAD_CREDENTIALS",
  "NOT_FOUND",
  "BOOK_NOT_AVAILABLE",
  "USER_BLOCKED",
  "LOAN_ALREADY_RETURNED",
  "DUPLICATE_RESERVATION",
  "EXTERNAL_LOOKUP_FAILED",
  "MISSING_BOOK_DATA",
  "DUPLICATE_ISBN",
  "BOOK_NOT_DELETABLE",
  "BOOK_NOT_RESTORABLE",
]);

function toApiErrorCode(code: string): ApiErrorCode {
  if (KNOWN_CODES.has(code)) {
    return code as ApiErrorCode;
  }
  return "UNKNOWN";
}

export async function parseApiError(
  response: Response,
): Promise<ApiException> {
  let payload: unknown;
  try {
    payload = await response.json();
  } catch {
    return new ApiException({
      status: response.status,
      code: "UNKNOWN",
      message: response.statusText || "Error inesperado",
    });
  }

  if (isApiErrorBody(payload)) {
    return new ApiException({
      status: payload.status,
      code: toApiErrorCode(payload.code),
      message: payload.message,
      path: payload.path,
      reservationsAhead: payload.reservationsAhead,
    });
  }

  return new ApiException({
    status: response.status,
    code: "UNKNOWN",
    message: response.statusText || "Error inesperado",
  });
}

export function toApiException(error: unknown): ApiException {
  if (error instanceof ApiException) {
    return error;
  }
  if (error instanceof Error) {
    return new ApiException({
      status: 0,
      code: "NETWORK_ERROR",
      message: error.message,
    });
  }
  return new ApiException({
    status: 0,
    code: "UNKNOWN",
    message: "Error inesperado",
  });
}
