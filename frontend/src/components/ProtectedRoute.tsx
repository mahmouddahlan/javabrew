import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import React from "react";

export default function ProtectedRoute({
  children,
  requireAdmin = false
}: {
  children: React.ReactElement;
  requireAdmin?: boolean;
}) {
  const { token, user } = useAuth();

  if (!token) return <Navigate to="/login" replace />;
  if (requireAdmin && user?.role !== "ADMIN") return <Navigate to="/catalogue" replace />;

  return children;
}