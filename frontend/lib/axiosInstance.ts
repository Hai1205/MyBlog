import { SERVER_URL } from "@/services/constants";
import axios, { InternalAxiosRequestConfig, AxiosResponse } from "axios";
import { toast } from "react-toastify";

export const MAX_RETRIES = 0;

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

const getCookie = (name: string): string | null => {
  const matches = document.cookie.match(new RegExp(`(^| )${name}=([^;]+)`));
  return matches ? matches[2] : null;
};

export const getRefreshToken = (): string | null => {
  return getCookie('refresh_token') ||
    localStorage.getItem('refresh_token') ||
    sessionStorage.getItem('refresh_token');
};

export const BASE_URL = `${SERVER_URL}/api/v1`;

const axiosInstance = axios.create({
  baseURL: BASE_URL,
  withCredentials: true,
  timeout: 30000, // Add a timeout to prevent hanging requests
});

const getAccessToken = (item: string): string | null => {
  // Try multiple cookie names (case variations)
  const token = getCookie(item) ||
    getCookie('access_token') ||
    localStorage.getItem(item) ||
    sessionStorage.getItem(item);

  return token;
};

// Function to refresh access token
const refreshAccessToken = async (): Promise<string | null> => {
  // Check if refresh token exists before attempting refresh
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    console.log('No refresh token available, cannot refresh access token');
    return null;
  }

  try {
    console.log('Attempting to refresh token...', {
      hasRefreshToken: !!refreshToken,
      refreshTokenLength: refreshToken?.length
    });

    const response = await axios.post(
      `${SERVER_URL}/api/v1/auth/refresh-token`,
      {},
      {
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );

    console.log('Token refreshed successfully', response.data);

    // Wait a bit to ensure cookie is set
    await new Promise(resolve => setTimeout(resolve, 100));

    return getCookie('access_token');
  } catch (error) {
    console.error('Token refresh failed:', error);

    // Chỉ xóa cookie khi server trả về lỗi 401 (invalid/expired token)
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      console.log('🔒 Token invalid or expired - clearing auth state');

      if (typeof window !== 'undefined') {
        document.cookie = 'access_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        localStorage.removeItem('refresh_token');
        sessionStorage.removeItem('refresh_token');
        window.location.href = '/auth/login';
      }
    } else {
      console.log('Server or network error, keeping tokens');
    }
    return null;
  }
};

axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken("access_token");

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    (config as { retryCount?: number }).retryCount = (config as { retryCount?: number }).retryCount || 0;

    return config;
  },
  (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },

  async (error: unknown) => {
    interface RetryConfig extends InternalAxiosRequestConfig {
      retryCount?: number;
      _retry?: boolean;
    }

    if (!axios.isAxiosError(error) || !error.config) {
      return Promise.reject(error);
    }

    const config = error.config as RetryConfig;

    // If 401 Unauthorized and not already retrying
    if (error.response?.status === 401 && !config._retry) {
      // Skip refresh for login/register/refresh-token endpoints
      if (config.url?.includes('/auth/login') ||
        config.url?.includes('/auth/register') ||
        config.url?.includes('/auth/refresh-token')) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // If already refreshing, queue this request
        console.log('⏳ Request queued while token is being refreshed:', config.url);
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(async () => {
            // Wait a bit to ensure token is properly set
            await new Promise(resolve => setTimeout(resolve, 100));
            const newToken = getAccessToken("access_token");
            console.log('🔓 Retrying queued request with new token:', config.url, {
              hasToken: !!newToken,
              tokenPreview: newToken ? newToken.substring(0, 20) + '...' : 'NONE'
            });
            if (newToken && config.headers) {
              config.headers.Authorization = `Bearer ${newToken}`;
            }
            return axiosInstance(config);
          })
          .catch((err) => {
            console.error('Queued request failed:', config.url, err);
            return Promise.reject(err);
          });
      }

      config._retry = true;
      isRefreshing = true;
      console.log('Starting token refresh due to 401 from:', config.url);

      try {
        const newToken = await refreshAccessToken();

        if (newToken && config.headers) {
          config.headers.Authorization = `Bearer ${newToken}`;
          console.log('Retrying original request with new token:', config.url);
        } else {
          console.error('No new token received, cannot retry request');
          processQueue(new Error('Token refresh failed'), null);
          isRefreshing = false;
          return Promise.reject(error);
        }

        processQueue(null, newToken);
        isRefreshing = false;

        // Add small delay to ensure cookie is set before retrying
        await new Promise(resolve => setTimeout(resolve, 200));

        return axiosInstance(config);
      } catch (refreshError) {
        console.error('Token refresh error:', refreshError);
        processQueue(refreshError, null);
        isRefreshing = false;
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export interface IApiResponse<IData = unknown> {
  data?: (IData & { message: string }) | null;
  status?: number;
  success?: boolean;
}

export enum EHttpType {
  GET = "GET",
  POST = "POST",
  PUT = "PUT",
  PATCH = "PATCH",
  DELETE = "DELETE",
}

interface IAxiosError {
  message?: string;
  status?: number;
}

export const handleRequest = async <T = unknown>(
  type: EHttpType,
  route: string,
  data?: FormData | Record<string, unknown>,
  toastMessage?: boolean
): Promise<IApiResponse<T>> => {
  let response;

  try {
    // Get token for Authorization header
    const token = getAccessToken("access_token");

    console.log('🔑 REQUEST DEBUG:', {
      route,
      hasToken: !!token,
      token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN',
      isFormData: data instanceof FormData
    });

    const headers: Record<string, string> = {};

    // Set Authorization header if token exists
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    // Set Content-Type based on data type (only for non-FormData)
    // if (data && !(data instanceof FormData)) {
    //   headers['Content-Type'] = 'application/json';
    // }
    // For FormData, let axios set Content-Type automatically with boundary

    // If sending FormData (file uploads), increase timeout because uploads can take longer
    const isForm = data instanceof FormData;
    const requestOptions: Record<string, unknown> = { headers };
    if (isForm) {
      // 60 seconds for uploads
      (requestOptions as any).timeout = 60000;
    }

    switch (type) {
      case EHttpType.GET:
        response = await axiosInstance.get(route, requestOptions as any);
        break;

      case EHttpType.POST:
        response = await axiosInstance.post(route, data, requestOptions as any);
        break;

      case EHttpType.PUT:
        if (!data) {
          throw new Error("Data is required for PUT requests");
        }
        response = await axiosInstance.put(route, data, requestOptions as any);
        break;

      case EHttpType.PATCH:
        if (!data) {
          throw new Error("Data is required for PATCH requests");
        }
        response = await axiosInstance.patch(route, data, requestOptions as any);
        break;

      case EHttpType.DELETE:
        response = await axiosInstance.delete(route, requestOptions as any);
        break;

      default:
        throw new Error("Invalid request type");
    }

    if (toastMessage) {
      toast.success(toastMessage);
    }

    return {
      status: response.status,
      data: response.data as (T & { message: string }),
      success: true
    };
  } catch (error: unknown) {
    console.error("Error fetching data:", error);

    // Type guard to check if error is an Axios error
    if (axios.isAxiosError(error) && error.response) {
      const axiosErrorData = error.response.data as IAxiosError;

      if (axiosErrorData?.message) {
        toast.error(axiosErrorData.message);
      }

      // Define a more specific type for the response data
      interface ErrorResponseData {
        data?: T | null;
        message?: string;
        success?: boolean;
        [key: string]: unknown;
      }

      const responseData = error.response.data as ErrorResponseData;

      return {
        status: error.response.status,
        data: responseData as (T & { message: string }),
        success: false
      };
    }

    // Handle non-Axios errors
    const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
    toast.error(errorMessage);

    return {
      status: 500,
      data: null,
    };
  }
};

export const isSuccess = (status?: number) => status && status >= 200 && status < 300;

// Export refresh function for manual refresh
export { refreshAccessToken };

export default axiosInstance;