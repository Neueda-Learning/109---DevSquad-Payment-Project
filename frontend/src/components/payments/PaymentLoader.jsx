import { useCallback, useEffect, useRef, useState } from "react";
import "./PaymentLoader.css";

const FOLD_MS = 900;
const LAUNCH_MS = 850;
const AIR_MS = 4200;
const CRASH_AT = 2400;
const ARRIVE_MS = 950;
const AIR_LABEL_MS = 700;
const CRASH_ANIM_MS = 1600;
const DONE_ANIM_MS = 650;

const AIR_LABELS = ["Validating payment…", "Processing…"];

/* ------------------------------ artwork ------------------------------ */

function Person({ side, id }) {
  const skin = side === "sender" ? "#e8b58c" : "#c98d63";
  const skinShade = side === "sender" ? "#d29a70" : "#ab714b";
  const hair = side === "sender" ? "#3a2b23" : "#20160f";
  const shirtA = side === "sender" ? "#3f6bb5" : "#8c4a63";
  const shirtB = side === "sender" ? "#2e4f8a" : "#6c364b";

  return (
    <svg viewBox="0 0 88 150" aria-hidden="true">
      <defs>
        <linearGradient id={`${id}-shirt`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor={shirtA} />
          <stop offset="1" stopColor={shirtB} />
        </linearGradient>
        <linearGradient id={`${id}-skin`} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor={skin} />
          <stop offset="1" stopColor={skinShade} />
        </linearGradient>
      </defs>

      <g transform={side === "receiver" ? "translate(88,0) scale(-1,1)" : undefined}>
        {/* legs */}
        <path d="M30 96h28l3 46h-12l-3-30-3 30H27z" fill="#2b3242" />
        <path d="M24 140h15v6H22a2 2 0 0 1-2-2c0-2 1.6-4 4-4Z" fill="#1a1f2b" />
        <path d="M44 140h15v6H42a2 2 0 0 1-2-2c0-2 1.6-4 4-4Z" fill="#1a1f2b" />

        {/* torso */}
        <path
          d="M31 48h26c7 0 12 5 12.5 12l2.5 30c.4 5-3 8-8 8H24c-5 0-8.4-3-8-8l2.5-30C19 53 24 48 31 48Z"
          fill={`url(#${id}-shirt)`}
        />
        <path d="M37 48h14l-7 9-7-9Z" fill="#ffffff" opacity="0.9" />
        <rect x="43" y="56" width="2" height="34" fill="#000" opacity="0.12" />

        {/* far arm */}
        <path d="M24 54c-4 3-6 8-6 14l1 20h7l-1-20 4-9-5-5Z" fill={shirtB} />

        {/* extended arm (animated) */}
        <g className="pl-arm">
          <path
            d="M56 54c6 0 10 3 12 6l14 3a4 4 0 0 1 0 8l-16 1c-5 .3-9-2-10-6l-3-8 3-4Z"
            fill={shirtA}
          />
          <path
            d="M68 60l13 2.4a4 4 0 0 1 0 7.9l-13 1.1a5.8 5.8 0 0 1 0-11.4Z"
            fill={`url(#${id}-skin)`}
          />
        </g>

        {/* neck + head */}
        <path d="M38 40h12v11H38z" fill={skinShade} />
        <ellipse cx="44" cy="26" rx="15" ry="17" fill={`url(#${id}-skin)`} />
        <ellipse cx="30" cy="27" rx="3" ry="4" fill={skinShade} />
        <path
          d="M29 22c0-9 6-14 15-14s15 5 15 14c0 3-1 5-1 5s-1-6-4-7c-4 3-16 4-21 1-2 1.5-3 4-3 7 0 0-1-3-1-6Z"
          fill={hair}
        />
        <ellipse cx="38" cy="27" rx="1.6" ry="2" fill="#2b2118" />
        <ellipse cx="50" cy="27" rx="1.6" ry="2" fill="#2b2118" />
        <path
          d="M35 22.5c1.6-1 3.6-1 5 .2"
          stroke={hair}
          strokeWidth="1.4"
          strokeLinecap="round"
          fill="none"
        />
        <path
          d="M47 22.7c1.4-1.2 3.4-1.2 5-.2"
          stroke={hair}
          strokeWidth="1.4"
          strokeLinecap="round"
          fill="none"
        />
        <path
          d="M40 34c2.4 1.8 5.6 1.8 8 0"
          stroke="#8c5a45"
          strokeWidth="1.6"
          strokeLinecap="round"
          fill="none"
        />
      </g>
    </svg>
  );
}

function Banknote({ amount }) {
  return (
    <svg viewBox="0 0 124 62" aria-hidden="true">
      <defs>
        <linearGradient id="pl-bill" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#d8f0d2" />
          <stop offset="0.5" stopColor="#b9e0ad" />
          <stop offset="1" stopColor="#8fc98a" />
        </linearGradient>
      </defs>
      <rect
        x="1"
        y="1"
        width="122"
        height="60"
        rx="4"
        fill="url(#pl-bill)"
        stroke="#3f7a45"
        strokeWidth="2"
      />
      <rect
        x="7"
        y="7"
        width="110"
        height="48"
        rx="3"
        fill="none"
        stroke="#4d8a52"
        strokeWidth="1"
        opacity="0.8"
      />
      <ellipse cx="62" cy="31" rx="17" ry="21" fill="#a6d69c" stroke="#4d8a52" strokeWidth="1.2" />
      <circle cx="62" cy="25" r="6.5" fill="#7fb87a" />
      <path d="M52 45c1.5-7 5.5-11 10-11s8.5 4 10 11Z" fill="#7fb87a" />
      <text x="15" y="22" fontSize="13" fontWeight="700" fill="#2f6b39" fontFamily="Georgia, serif">
        {amount}
      </text>
      <text
        x="109"
        y="48"
        fontSize="13"
        fontWeight="700"
        fill="#2f6b39"
        fontFamily="Georgia, serif"
        textAnchor="end"
      >
        {amount}
      </text>
      <circle cx="20" cy="44" r="7" fill="none" stroke="#4d8a52" strokeWidth="1" opacity="0.55" />
      <circle cx="104" cy="18" r="7" fill="none" stroke="#4d8a52" strokeWidth="1" opacity="0.55" />
    </svg>
  );
}

function PaperPlane() {
  return (
    <svg viewBox="0 0 124 62" aria-hidden="true">
      <path
        d="M6 31 118 6 86 56 62 40 6 31Z"
        fill="#dff0d8"
        stroke="#3f7a45"
        strokeWidth="2"
        strokeLinejoin="round"
      />
      <path
        d="M62 40 118 6 86 56 62 40Z"
        fill="#9ccb93"
        stroke="#3f7a45"
        strokeWidth="2"
        strokeLinejoin="round"
      />
      <path d="M62 40 118 6" stroke="#3f7a45" strokeWidth="2" strokeLinecap="round" />
      <path
        d="M62 40v16l10-9"
        fill="#c3e3b9"
        stroke="#3f7a45"
        strokeWidth="2"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function Flyer({ amount, isPlane }) {
  return (
    <div className={`pl-flyer${isPlane ? " pl-is-plane" : ""}`}>
      <span className="pl-face pl-note">
        <Banknote amount={amount} />
      </span>
      <span className="pl-face pl-plane">
        <PaperPlane />
      </span>
    </div>
  );
}

/* ------------------------------ component ------------------------------ */

/**
 * Payment processing animation with staged progress:
 * Creating -> Processing -> Validating -> Processing -> Failed/Completed
 *
 * Props:
 * - amount: string printed on the banknote
 * - status: "idle" | "processing" | "success" | "error" (controlled). Omit for demo mode.
 * - senderName, receiverName: labels under each character
 * - onSettled(phase): called once when the animation reaches "done" or "crash"
 */
export default function PaymentLoader({
  amount = "$100",
  status,
  senderName = "Sender",
  receiverName = "Receiver",
  onSettled,
}) {
  const [phase, setPhase] = useState("idle");
  const [airLabelIdx, setAirLabelIdx] = useState(0);
  const timers = useRef([]);

  const clearTimers = () => {
    timers.current.forEach(clearTimeout);
    timers.current = [];
  };
  const later = (fn, ms) => {
    timers.current.push(setTimeout(fn, ms));
  };

  const run = useCallback((outcome) => {
    clearTimers();
    setPhase("idle");
    let t = 80;
    later(() => setPhase("fold"), t);
    t += FOLD_MS;
    later(() => setPhase("launch"), t);
    t += LAUNCH_MS;
    later(() => setPhase("air"), t);
    if (outcome === "success") {
      t += AIR_MS;
      later(() => setPhase("arrive"), t);
      t += ARRIVE_MS;
      later(() => setPhase("done"), t);
    } else {
      later(() => setPhase("crash"), t + CRASH_AT);
    }
  }, []);

  useEffect(() => {
    if (!status) return;
    clearTimers();
    if (status === "processing") {
      setPhase("fold");
      later(() => setPhase("launch"), FOLD_MS);
      later(() => setPhase("air"), FOLD_MS + LAUNCH_MS);
    } else if (status === "success") {
      setPhase("arrive");
      later(() => setPhase("done"), ARRIVE_MS);
    } else if (status === "error") {
      // Always show the plane mid-flight (frame 2) before crashing,
      // even if the failure happened before reaching the air phase.
      setPhase("air");
      later(() => setPhase("crash"), CRASH_AT);
    } else {
      setPhase("idle");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status]);

  useEffect(() => clearTimers, []);

  // Cycle "Validating…" / "Processing…" labels while in transit.
  useEffect(() => {
    if (phase !== "air") {
      setAirLabelIdx(0);
      return;
    }
    const id = setInterval(() => {
      setAirLabelIdx((i) => (i + 1) % AIR_LABELS.length);
    }, AIR_LABEL_MS);
    return () => clearInterval(id);
  }, [phase]);

  // Notify parent once the animation has settled on a final outcome,
  // waiting for the corresponding CSS animation to finish playing.
  useEffect(() => {
    if (phase === "done") {
      later(() => onSettled?.("done"), DONE_ANIM_MS);
    } else if (phase === "crash") {
      later(() => onSettled?.("crash"), CRASH_ANIM_MS);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [phase]);

  const scene =
    phase === "air" || phase === "crash" ? "air" : phase === "arrive" || phase === "done" ? "recv" : "send";

  const tone =
    phase === "done" ? "ok" : phase === "crash" ? "fail" : phase === "idle" ? "idle" : "pending";

  const message =
    phase === "idle"
      ? `Ready to send ${amount}`
      : phase === "fold"
        ? "Creating payment…"
        : phase === "launch"
          ? "Processing…"
          : phase === "air"
            ? AIR_LABELS[airLabelIdx]
            : phase === "crash"
              ? "Payment failed"
              : phase === "arrive"
                ? "Processing…"
                : `Completed — ${amount} delivered to ${receiverName}`;

  const busy = phase !== "idle" && phase !== "done" && phase !== "crash";
  const stepIndex = scene === "send" ? 0 : scene === "air" ? 1 : 2;

  return (
    <div className="pl-wrap" data-phase={phase} data-scene={scene}>
      <div className="pl-stage">
        {/* frame 1 — sender */}
        <div className="pl-scene pl-scene-send">
          <span className="pl-frame-tag">1 · Creating</span>
          <span className="pl-ground" />
          <div className="pl-person">
            <span className="pl-shadow" />
            <Person side="sender" id="pl-s" />
            <span className="pl-label">{senderName}</span>
          </div>
          <Flyer amount={amount} isPlane={phase === "fold" || phase === "launch"} />
        </div>

        {/* frame 2 — open air */}
        <div className="pl-scene pl-scene-air">
          <span className="pl-frame-tag">2 · Validating</span>
          <span className="pl-ground pl-ground-air" />
          <span className="pl-cloud" />
          <span className="pl-cloud" />
          <span className="pl-wind" />
          <span className="pl-wind" />
          <span className="pl-wind" />
          <span className="pl-wind" />
          <span className="pl-wind" />
          <span className="pl-wind" />
          <Flyer amount={amount} isPlane />
          <span className="pl-smoke" />
          <span className="pl-smoke" />
          <span className="pl-smoke" />
        </div>

        {/* frame 3 — receiver */}
        <div className="pl-scene pl-scene-recv">
          <span className="pl-frame-tag">3 · Completed</span>
          <span className="pl-ground" />
          <div className="pl-person">
            <span className="pl-shadow" />
            <Person side="receiver" id="pl-r" />
            <span className="pl-label">{receiverName}</span>
          </div>
          <Flyer amount={amount} isPlane={phase === "arrive"} />
        </div>
      </div>

      <div className="pl-steps" aria-hidden="true">
        {[0, 1, 2].map((i) => (
          <span key={i} className="pl-step" data-active={i <= stepIndex} />
        ))}
      </div>

      <p className="pl-status" data-tone={tone} role="status">
        <span className="pl-dot" />
        {message}
      </p>

      {!status && (
        <div className="pl-actions">
          <button className="pl-btn" onClick={() => run("success")} disabled={busy}>
            Send (succeeds)
          </button>
          <button className="pl-btn" onClick={() => run("error")} disabled={busy}>
            Send (fails)
          </button>
        </div>
      )}
    </div>
  );
}
