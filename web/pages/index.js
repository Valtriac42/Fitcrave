import { useEffect, useState } from "react";
import { createClient } from "@supabase/supabase-js";

// Bumped each release so the in-page download button always points at the latest APK.
// Override at deploy time with NEXT_PUBLIC_APK_URL if you upload elsewhere.
const APK_URL =
  process.env.NEXT_PUBLIC_APK_URL ||
  "https://github.com/Valtriac42/Fitcrave/releases/latest/download/fitcrave.apk";

const supabaseUrl  = (process.env.NEXT_PUBLIC_SUPABASE_URL  || "").trim();
const supabaseAnon = (process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || "").trim();

export default function Home() {
  const [stats, setStats] = useState({ workouts: 0, kcal: 0, minutes: 0 });
  const [showApk, setShowApk] = useState(false);
  const [dismissed, setDismissed] = useState(false);

  useEffect(() => {
    // Android detection — pops the download CTA only on Android phones / tablets.
    const ua = navigator.userAgent || "";
    if (/Android/i.test(ua) && !/Fitcrave-WebView/i.test(ua)) {
      setShowApk(true);
    }
  }, []);

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
        <h2 style={{ marginTop: 0 }}>This week</h2>
        <p style={styles.muted}>
          Your full reports, progress charts and history will appear here.
        </p>
        <a href={APK_URL} style={styles.ctaSecondary}>Download Android APK</a>
      </section>

      {showApk && !dismissed && (
        <div style={styles.banner} role="dialog" aria-label="Install Fitcrave">
          <div style={styles.bannerInner}>
            <div style={styles.bannerIcon}>F</div>
            <div style={{ flex: 1 }}>
              <div style={styles.bannerTitle}>Get the Fitcrave app</div>
              <div style={styles.bannerSub}>
                Workouts, diet plans and reports — installed in one tap.
              </div>
            </div>
            <a href={APK_URL} style={styles.bannerBtn}>Install</a>
            <button
              type="button"
              onClick={() => setDismissed(true)}
              aria-label="Dismiss"
              style={styles.bannerClose}
            >
              ×
            </button>
          </div>
        </div>
      )}
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
    paddingBottom: 120,
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
  ctaSecondary: {
    display: "inline-block",
    marginTop: 12,
    background: "#D94B4B",
    color: "#fff",
    padding: "10px 18px",
    borderRadius: 24,
    textDecoration: "none",
    fontWeight: 600,
    fontSize: 14,
  },
  banner: {
    position: "fixed",
    left: 12,
    right: 12,
    bottom: 12,
    background: "#1A1A1A",
    color: "#fff",
    borderRadius: 16,
    boxShadow: "0 10px 30px rgba(0,0,0,0.25)",
    zIndex: 50,
  },
  bannerInner: {
    display: "flex",
    alignItems: "center",
    gap: 12,
    padding: "12px 14px",
  },
  bannerIcon: {
    width: 40,
    height: 40,
    borderRadius: 10,
    background: "#D94B4B",
    color: "#fff",
    display: "grid",
    placeItems: "center",
    fontWeight: 800,
    fontSize: 18,
  },
  bannerTitle: { fontSize: 15, fontWeight: 700 },
  bannerSub: { fontSize: 12, color: "#bdbdbd", marginTop: 2 },
  bannerBtn: {
    background: "#D94B4B",
    color: "#fff",
    padding: "10px 16px",
    borderRadius: 22,
    textDecoration: "none",
    fontWeight: 700,
    fontSize: 13,
  },
  bannerClose: {
    background: "transparent",
    color: "#bdbdbd",
    border: "none",
    fontSize: 22,
    cursor: "pointer",
    padding: "0 4px",
    lineHeight: 1,
  },
};
