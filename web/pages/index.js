import { useEffect, useState } from "react";
import { createClient } from "@supabase/supabase-js";

// Read env at build time, but instantiate the client lazily inside useEffect
// so a bad/missing value can't break static rendering.
const supabaseUrl  = (process.env.NEXT_PUBLIC_SUPABASE_URL  || "").trim();
const supabaseAnon = (process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || "").trim();

export default function Home() {
  const [stats, setStats] = useState({ workouts: 0, kcal: 0, minutes: 0 });

  useEffect(() => {
    if (!supabaseUrl || !supabaseAnon) return;
    let supabase;
    try {
      supabase = createClient(supabaseUrl, supabaseAnon);
    } catch (e) {
      console.error("Invalid Supabase config:", e);
      return;
    }
    (async () => {
      const today = new Date().toISOString().slice(0, 10);
      const { data } = await supabase
        .from("daily_stats")
        .select("workouts_completed,kcal,minutes")
        .eq("day_date", today)
        .limit(1)
        .maybeSingle();
      if (data) {
        setStats({
          workouts: data.workouts_completed ?? 0,
          kcal: data.kcal ?? 0,
          minutes: data.minutes ?? 0,
        });
      }
    })();
  }, []);

  return (
    <main style={styles.page}>
      <header style={styles.header}>
        <h1 style={styles.logo}>Fitcrave</h1>
        <span style={styles.tag}>Web Dashboard</span>
      </header>
      <section style={styles.statsRow}>
        <Stat label="Workouts" value={stats.workouts} />
        <Stat label="KCAL"     value={stats.kcal} />
        <Stat label="Minutes"  value={stats.minutes} />
      </section>
      <section style={styles.card}>
        <h2>This week</h2>
        <p style={styles.muted}>
          Your full reports, progress charts and history will appear here.
        </p>
      </section>
    </main>
  );
}

function Stat({ label, value }) {
  return (
    <div style={styles.stat}>
      <div style={styles.statValue}>{value}</div>
      <div style={styles.statLabel}>{label}</div>
    </div>
  );
}

const styles = {
  page: {
    fontFamily: "system-ui, -apple-system, Segoe UI, Roboto, sans-serif",
    background: "#F2F2F4",
    minHeight: "100vh",
    margin: 0,
    padding: 20,
    color: "#1A1A1A",
  },
  header: { display: "flex", alignItems: "center", gap: 12, marginBottom: 20 },
  logo: { color: "#D94B4B", margin: 0, fontSize: 28 },
  tag: { color: "#6B6B6B", fontSize: 13 },
  statsRow: {
    display: "grid",
    gridTemplateColumns: "repeat(3,1fr)",
    gap: 10,
    marginBottom: 16,
  },
  stat: {
    background: "#fff",
    borderRadius: 14,
    padding: 16,
    textAlign: "center",
  },
  statValue: { fontSize: 28, fontWeight: 300 },
  statLabel: { fontSize: 12, color: "#6B6B6B", marginTop: 4 },
  card: { background: "#fff", borderRadius: 18, padding: 20 },
  muted: { color: "#6B6B6B" },
};
