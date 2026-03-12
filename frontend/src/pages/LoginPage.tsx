import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const { login } = useAuth();  // remove refreshMe here
  const navigate = useNavigate();

  const [username, setUsername] = useState("alice1");
  const [password, setPassword] = useState("Passw0rd!");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const data = await login(username.trim(), password.trim());
      // Navigate based on role from login response directly
      // Don't call refreshMe() - the useEffect in AuthContext handles it
      if (data.role === "ADMIN") {
        navigate("/admin");
      } else {
        navigate("/catalogue");
      }
    } catch (err: any) {
      setError(err?.response?.data?.message ?? err?.message ?? "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="card">
      <h2>Login</h2>
      {error && <div className="error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <label>Username</label>
        <input
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
        />
        <label>Password</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
        />
        <button type="submit" disabled={loading}>
          {loading ? "Logging in…" : "Login"}
        </button>
      </form>
      <p>No account? <Link to="/signup">Create one</Link></p>
    </div>
  );
}