import { getApiBaseUrl } from "./config";
import { parseApiError, toApiException } from "./errors";
import { getAccessToken } from "./token";

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

export type QueryValue = string | number | boolean | null | undefined;

export type ApiRequestOptions = {
  method?: HttpMethod;
  body?: unknown;
  query?: Record<string, QueryValue>;
  auth?: boolean;
};

function buildRequestUrl(
  path: string,
  query?: Record<string, QueryValue>,
): string {
  const base = getApiBaseUrl().replace(/\/$/, "");
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const isAbsolute = base.startsWith("http://") || base.startsWith("https://");
  const url = isAbsolute
    ? new URL(`${base}${normalizedPath}`)
    : new URL(`${base}${normalizedPath}`, "http://local.invalid");

  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value === null || value === undefined || value === "") {
        continue;
      }
      url.searchParams.set(key, String(value));
    }
  }

  return isAbsolute ? url.toString() : `${url.pathname}${url.search}`;
}

export async function apiClient<T>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<T> {
  const { method = "GET", body, query, auth = true } = options;
  const requestUrl = buildRequestUrl(path, query);
  const headers = new Headers();

  if (body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  if (auth) {
    const token = getAccessToken();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  let response: Response;
  try {
    response = await fetch(requestUrl, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (error) {
    throw toApiException(error);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  if (!response.ok) {
    throw await parseApiError(response);
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
