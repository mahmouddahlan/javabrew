import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { api, setAuthToken } from "../api/client";
import type { CurrentUserResponse, LoginResponse, SignupRequest } from "../types/api";

type AuthContextType = {
  token: string | null;
  user: CurrentUserResponse | null;
  login: (username: string, password: string) => Promise<LoginResponse>;
  signup: (payload: SignupRequest) => Promise<void>;
  logout: () => void;
  refreshMe: () => Promise<CurrentUserResponse>;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(localStorage.getItem("token"));
  const [user, setUser] = useState<CurrentUserResponse | null>(null);

  // On mount: if token exists in storage, re-hydrate axios + fetch user
  useEffect(() => {
    const stored = localStorage.getItem("token");
    if (stored) {
      setAuthToken(stored);
      api.get<CurrentUserResponse>("/auth/me")
        .then(({ data }) => setUser(data))
        .catch(() => {
          // token is dead, clean up
          localStorage.removeItem("token");
          setToken(null);
          setAuthToken(null);
        });
    }
  }, []);

  useEffect(() => {
    if (token && !user) {
      api.get<CurrentUserResponse>("/auth/me")
        .then(({ data }) => setUser(data))
        .catch(() => {
          localStorage.removeItem("token");
          setToken(null);
          setAuthToken(null);
        });
    }
  }, [token]); // runs whenever token changes
  
  async function login(username: string, password: string): Promise<LoginResponse> {
    console.log("LOGIN REQUEST:", { username, password });

    const { data } = await api.post<LoginResponse>("/auth/login", { username, password });
    console.log("LOGIN RESPONSE:", data);

    // Set token on axios FIRST, synchronously, before any state updates
    setAuthToken(data.token);
    localStorage.setItem("token", data.token);
    setToken(data.token);

    return data;
  }

  async function signup(payload: SignupRequest) {
    await api.post("/auth/signup", payload);
  }

  async function refreshMe(): Promise<CurrentUserResponse> {
    // At this point setAuthToken has already been called, so this will work
    const { data } = await api.get<CurrentUserResponse>("/auth/me");
    setUser(data);
    return data;
  }

  function logout() {
    setAuthToken(null);
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
  }

  const value = useMemo(
    () => ({ token, user, login, signup, logout, refreshMe }),
    [token, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}

