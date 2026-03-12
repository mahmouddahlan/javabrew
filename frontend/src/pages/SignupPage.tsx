import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function SignupPage() {
  const { signup } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: "",
    password: "",
    firstName: "",
    lastName: "",
    streetName: "",
    streetNumber: "",
    city: "",
    country: "",
    postalCode: ""
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  function update(key: string, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");

    try {
      await signup({
        username: form.username,
        password: form.password,
        firstName: form.firstName,
        lastName: form.lastName,
        shippingAddress: {
          streetName: form.streetName,
          streetNumber: form.streetNumber,
          city: form.city,
          country: form.country,
          postalCode: form.postalCode
        }
      });

      setSuccess("Signup successful. Redirecting to login...");
      setTimeout(() => navigate("/login"), 1000);
    } catch (err: any) {
      setError(err?.response?.data?.message || "Signup failed");
    }
  }

  return (
    <div className="card">
      <h2>Signup</h2>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}

      <form onSubmit={handleSubmit}>
        <div className="grid grid-2">
          <div>
            <label>Username</label>
            <input value={form.username} onChange={(e) => update("username", e.target.value)} />
          </div>
          <div>
            <label>Password</label>
            <input type="password" value={form.password} onChange={(e) => update("password", e.target.value)} />
          </div>
          <div>
            <label>First Name</label>
            <input value={form.firstName} onChange={(e) => update("firstName", e.target.value)} />
          </div>
          <div>
            <label>Last Name</label>
            <input value={form.lastName} onChange={(e) => update("lastName", e.target.value)} />
          </div>
          <div>
            <label>Street Name</label>
            <input value={form.streetName} onChange={(e) => update("streetName", e.target.value)} />
          </div>
          <div>
            <label>Street Number</label>
            <input value={form.streetNumber} onChange={(e) => update("streetNumber", e.target.value)} />
          </div>
          <div>
            <label>City</label>
            <input value={form.city} onChange={(e) => update("city", e.target.value)} />
          </div>
          <div>
            <label>Country</label>
            <input value={form.country} onChange={(e) => update("country", e.target.value)} />
          </div>
          <div>
            <label>Postal Code</label>
            <input value={form.postalCode} onChange={(e) => update("postalCode", e.target.value)} />
          </div>
        </div>

        <button type="submit">Create Account</button>
      </form>

      <p>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}