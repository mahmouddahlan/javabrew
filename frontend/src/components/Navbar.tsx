import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { token, user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="navbar">
      <div><Link to="/catalogue">JavaBrew</Link></div>

      <div className="nav-links">
        {token && <Link to="/catalogue">Catalogue</Link>}
        {token && <Link to="/create-item">Create Item</Link>}
        {user?.role === "ADMIN" && <Link to="/admin">Admin</Link>}

        {!token && <Link to="/login">Login</Link>}
        {!token && <Link to="/signup">Signup</Link>}

        {token && (
          <>
            {/* Show username + role once loaded, or just "Loading..." */}
            <span>
              {user ? `${user.username} (${user.role})` : "Loading..."}
            </span>
            <button
              className="secondary"
              style={{ width: "auto", margin: 0 }}
              onClick={() => { logout(); navigate("/login"); }}
            >
              Logout
            </button>
          </>
        )}
      </div>
    </div>
  );
}