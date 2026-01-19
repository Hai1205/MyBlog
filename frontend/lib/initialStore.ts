import { create } from "zustand";
import { persist, createJSONStorage, PersistOptions, PersistStorage } from "zustand/middleware";
import Cookies from 'js-cookie';

export interface IBaseStore {
  handleRequest: <R>(apiCall: () => Promise<R>) => Promise<R>;
  reset: () => void;
}

type TVariables = Record<string, unknown>;

export enum EStorageType {
  LOCAL = "LOCAL",
  SESSION = "SESSION",
  COOKIE = "COOKIE",
}

const createCookieStorage = <T>(): PersistStorage<T> => ({
  getItem: (name: string) => {
    if (typeof window === 'undefined') return null;
    try {
      const value = Cookies.get(name);
      return value ? JSON.parse(value) : null;
    } catch {
      return null;
    }
  },
  setItem: (name: string, value: any) => {
    if (typeof window === 'undefined') return;
    try {
      Cookies.set(name, JSON.stringify(value), { expires: 7 });
    } catch {
    }
  },
  removeItem: (name: string) => {
    if (typeof window === 'undefined') return;
    Cookies.remove(name);
  },
});

export function createStore<T extends IBaseStore, U = TVariables>(
  storeName: string,
  initialState: TVariables,
  storeActions: (set: (state: Partial<T>) => void, get: () => T) => U,
  options?: {
    persistOptions?: Partial<PersistOptions<T>>;
    storageType?: EStorageType;
  }
) {
  const storageType = options?.storageType ?? EStorageType.SESSION;
  const storage = (() => {
    if (typeof window === 'undefined') {
      return {
        getItem: () => null,
        setItem: () => { },
        removeItem: () => { },
      } as PersistStorage<T>;
    }

    switch (storageType) {
      case EStorageType.SESSION:
        return createJSONStorage<T>(() => sessionStorage);
      case EStorageType.COOKIE:
        return createCookieStorage<T>();
      case EStorageType.LOCAL:
      default:
        return createJSONStorage<T>(() => localStorage);
    }
  })();

  return create<T>()(
    persist(
      (set, get) => {
        const reset = () => {
          set({ ...initialState } as TVariables as T);
        };

        return {
          ...initialState,
          ...storeActions((state) => set(state as T), get as () => T),
          reset,
        } as unknown as T;
      },
      {
        name: `${storeName}-storage`,
        storage,
        partialize: (state) => {
          const { ...rest } = state as T;
          return rest as TVariables as T;
        },
        ...options?.persistOptions,
      }
    )
  );
}
