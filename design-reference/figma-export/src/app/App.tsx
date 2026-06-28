import { useState } from "react";
import {
  LayoutDashboard, Train, MapPin, Megaphone, Handshake,
  Trophy, Gift, BarChart2, ShieldCheck, Settings, ChevronLeft,
  ChevronRight, LogOut, Bell, Search, Plus, Eye, Pencil, Trash2,
  X, Upload, Copy, Check, AlertTriangle, RefreshCw, EyeOff,
  Download, Ticket, TrendingUp, Activity, Shield, ArrowRight,
  CheckCircle2, XCircle, Clock, AlertCircle, Stamp, Building2,
  Wifi, WifiOff, QrCode, NfcIcon, CalendarClock, PackageX,
  PackageCheck, Info, TriangleAlert, CircleAlert,
} from "lucide-react";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from "recharts";

// ─── Types ───────────────────────────────────────────────────────────────────

type Screen =
  | "login" | "dashboard" | "metro-lines" | "metro-line-detail"
  | "stations" | "station-detail" | "campaigns" | "campaign-detail"
  | "stamp-designs" | "partners" | "milestones" | "rewards"
  | "analytics" | "rbac" | "settings";

// ─── Sample Data ──────────────────────────────────────────────────────────────

const METRO_LINES = [
  { id: "1", code: "ML1", name: "Metro Line 1", displayName: "Tuyến số 1", color: "#009B3A", stations: 14, readyStations: 11, status: "ACTIVE", sortOrder: 1, updatedAt: "2024-06-10", updatedBy: "admin@exoticstamp.vn" },
  { id: "2", code: "ML2", name: "Metro Line 2", displayName: "Tuyến số 2", color: "#01599D", stations: 0, readyStations: 0, status: "DRAFT", sortOrder: 2, updatedAt: "2024-05-20", updatedBy: "admin@exoticstamp.vn" },
  { id: "3", code: "ML3A", name: "Metro Line 3A", displayName: "Tuyến số 3A", color: "#E83B28", stations: 0, readyStations: 0, status: "INACTIVE", sortOrder: 3, updatedAt: "2024-04-15", updatedBy: "ops@exoticstamp.vn" },
];

const STATIONS = [
  { id: "1",  code: "BT",   name: "Bến Thành",           line: "ML1", lineColor: "#009B3A", address: "Quận 1, TP.HCM",       status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 4821, lastScanAt: "2024-06-25 14:32", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.7724", lng: "106.6980", radius: 80 },
  { id: "2",  code: "NHT",  name: "Nhà hát Thành phố",   line: "ML1", lineColor: "#009B3A", address: "Quận 1, TP.HCM",       status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 3104, lastScanAt: "2024-06-25 13:51", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.7769", lng: "106.7030", radius: 80 },
  { id: "3",  code: "BS",   name: "Ba Son",               line: "ML1", lineColor: "#009B3A", address: "Quận 1, TP.HCM",       status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 2876, lastScanAt: "2024-06-25 12:44", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.7880", lng: "106.7080", radius: 100 },
  { id: "4",  code: "VT",   name: "Văn Thánh",            line: "ML1", lineColor: "#009B3A", address: "Bình Thạnh, TP.HCM",   status: "ACTIVE",   scanKeyStatus: "MISSING",    gpsReady: false, collectors: 1932, lastScanAt: "—",               updatedAt: "2024-05-10", updatedBy: "ops@exoticstamp.vn",   lat: "—",        lng: "—",        radius: 100 },
  { id: "5",  code: "TC",   name: "Tân Cảng",             line: "ML1", lineColor: "#009B3A", address: "Bình Thạnh, TP.HCM",   status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 2210, lastScanAt: "2024-06-25 11:20", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.7962", lng: "106.7180", radius: 100 },
  { id: "6",  code: "TD",   name: "Thảo Điền",            line: "ML1", lineColor: "#009B3A", address: "Quận 2, TP.HCM",       status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 1887, lastScanAt: "2024-06-25 10:08", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.8077", lng: "106.7360", radius: 100 },
  { id: "7",  code: "AP",   name: "An Phú",               line: "ML1", lineColor: "#009B3A", address: "Quận 2, TP.HCM",       status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 1543, lastScanAt: "2024-06-25 09:35", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.8010", lng: "106.7490", radius: 100 },
  { id: "8",  code: "RC",   name: "Rạch Chiếc",           line: "ML1", lineColor: "#009B3A", address: "Quận 9, TP.HCM",       status: "ACTIVE",   scanKeyStatus: "MISSING",    gpsReady: true,  collectors: 987,  lastScanAt: "—",               updatedAt: "2024-05-15", updatedBy: "ops@exoticstamp.vn",   lat: "10.8140", lng: "106.7650", radius: 100 },
  { id: "9",  code: "PL",   name: "Phước Long",           line: "ML1", lineColor: "#009B3A", address: "Quận 9, TP.HCM",       status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 1124, lastScanAt: "2024-06-24 18:00", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.8270", lng: "106.7780", radius: 100 },
  { id: "10", code: "BT2",  name: "Bình Thái",            line: "ML1", lineColor: "#009B3A", address: "Quận 9, TP.HCM",       status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: false, collectors: 876,  lastScanAt: "—",               updatedAt: "2024-05-20", updatedBy: "ops@exoticstamp.vn",   lat: "—",        lng: "—",        radius: 100 },
  { id: "11", code: "THD",  name: "Thủ Đức",              line: "ML1", lineColor: "#009B3A", address: "Thủ Đức, TP.HCM",      status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 1342, lastScanAt: "2024-06-25 08:10", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.8490", lng: "106.7920", radius: 120 },
  { id: "12", code: "KCNC", name: "Khu Công Nghệ Cao",   line: "ML1", lineColor: "#009B3A", address: "Thủ Đức, TP.HCM",      status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 654,  lastScanAt: "2024-06-24 16:44", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.8630", lng: "106.8010", radius: 120 },
  { id: "13", code: "ST",   name: "Suối Tiên",            line: "ML1", lineColor: "#009B3A", address: "Thủ Đức, TP.HCM",      status: "ACTIVE",   scanKeyStatus: "CONFIGURED", gpsReady: true,  collectors: 1089, lastScanAt: "2024-06-25 07:25", updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn", lat: "10.8750", lng: "106.8120", radius: 120 },
  { id: "14", code: "BXMD", name: "Bến Xe Miền Đông",    line: "ML1", lineColor: "#009B3A", address: "Thủ Đức, TP.HCM",      status: "DRAFT",    scanKeyStatus: "MISSING",    gpsReady: false, collectors: 0,    lastScanAt: "—",               updatedAt: "2024-06-10", updatedBy: "admin@exoticstamp.vn", lat: "—",        lng: "—",        radius: 120 },
];

const CAMPAIGNS = [
  { id: "1", code: "SUM24",  name: "Summer Metro 2024",      displayName: "Hè Metro 2024",           type: "SEASONAL", status: "ACTIVE",   startDate: "2024-06-01", endDate: "2024-08-31", priority: 1, stations: 12, updatedAt: "2024-06-01", updatedBy: "admin@exoticstamp.vn" },
  { id: "2", code: "STD001", name: "Standard Collection",    displayName: "Bộ sưu tập cơ bản",       type: "STANDARD", status: "ACTIVE",   startDate: "2024-01-01", endDate: "2024-12-31", priority: 2, stations: 14, updatedAt: "2024-01-01", updatedBy: "admin@exoticstamp.vn" },
  { id: "3", code: "NOEL24", name: "Christmas Special 2024", displayName: "Giáng Sinh 2024",         type: "EVENT",    status: "DRAFT",    startDate: "2024-12-20", endDate: "2024-12-31", priority: 1, stations: 0,  updatedAt: "2024-06-15", updatedBy: "ops@exoticstamp.vn" },
  { id: "4", code: "TET24",  name: "Tết 2024",               displayName: "Tết Nguyên Đán 2024",    type: "EVENT",    status: "ARCHIVED", startDate: "2024-02-08", endDate: "2024-02-15", priority: 1, stations: 14, updatedAt: "2024-02-16", updatedBy: "admin@exoticstamp.vn" },
];

const STAMP_DESIGNS = [
  { id: "1", name: "Bến Thành Classic",  campaign: "Standard Collection",    station: "Bến Thành",         rarity: "COMMON",    status: "ACTIVE", sortOrder: 1, updatedAt: "2024-06-01" },
  { id: "2", name: "Metro Dragon",       campaign: "Summer Metro 2024",      station: "Ba Son",            rarity: "LEGENDARY", status: "ACTIVE", sortOrder: 1, updatedAt: "2024-06-02" },
  { id: "3", name: "Saigon River View",  campaign: "Summer Metro 2024",      station: "Tân Cảng",          rarity: "RARE",      status: "ACTIVE", sortOrder: 2, updatedAt: "2024-06-02" },
  { id: "4", name: "Tech Hub Stamp",     campaign: "Standard Collection",    station: "Khu Công Nghệ Cao", rarity: "EPIC",      status: "DRAFT",  sortOrder: 5, updatedAt: "2024-06-10" },
  { id: "5", name: "Thảo Điền Vibe",    campaign: "Summer Metro 2024",      station: "Thảo Điền",         rarity: "COMMON",    status: "ACTIVE", sortOrder: 3, updatedAt: "2024-06-02" },
  { id: "6", name: "Terminal Beast",     campaign: "Christmas Special 2024", station: "Bến Xe Miền Đông",  rarity: "RARE",      status: "DRAFT",  sortOrder: 1, updatedAt: "2024-06-15" },
];

const PARTNERS = [
  { id: "1", name: "Grab Vietnam",      email: "partner@grab.com",              contractStart: "2024-01-01", contractEnd: "2024-12-31", active: true  },
  { id: "2", name: "Highlands Coffee",  email: "partner@highlandscoffee.vn",    contractStart: "2024-03-01", contractEnd: "2024-09-30", active: true  },
  { id: "3", name: "VinMart+",          email: "partner@vinmart.com",           contractStart: "2024-01-01", contractEnd: "2024-06-30", active: false },
  { id: "4", name: "Circle K Vietnam",  email: "partner@circlek.vn",            contractStart: "2024-06-01", contractEnd: "2025-05-31", active: true  },
];

const MILESTONES = [
  { id: "1", code: "MS3",  name: "First Trio",      campaign: "Standard Collection", requiredStamps: 3,  rewardType: "DIGITAL_STICKER", rewardTitle: "Explorer Badge",    status: "ACTIVE", sortOrder: 1 },
  { id: "2", code: "MS7",  name: "Week Warrior",    campaign: "Standard Collection", requiredStamps: 7,  rewardType: "VOUCHER",         rewardTitle: "Coffee Voucher 20k", status: "ACTIVE", sortOrder: 2 },
  { id: "3", code: "MS14", name: "Line Master",     campaign: "Standard Collection", requiredStamps: 14, rewardType: "VOUCHER",         rewardTitle: "Grab Voucher 50k",  status: "ACTIVE", sortOrder: 3 },
  { id: "4", code: "SUM7", name: "Summer Explorer", campaign: "Summer Metro 2024",   requiredStamps: 7,  rewardType: "BONUS_STAMP",     rewardTitle: "Bonus Stamp ×3",    status: "ACTIVE", sortOrder: 1 },
];

const REWARDS = [
  { id: "1", name: "Coffee Voucher 20k",     type: "VOUCHER",         partner: "Highlands Coffee", milestone: "Week Warrior",    value: 20000, expiryDays: 30, totalStock: 500,  issued: 312, active: true  },
  { id: "2", name: "Grab Voucher 50k",       type: "VOUCHER",         partner: "Grab Vietnam",     milestone: "Line Master",     value: 50000, expiryDays: 60, totalStock: 200,  issued: 187, active: true  },
  { id: "3", name: "Explorer Badge",         type: "DIGITAL_STICKER", partner: "—",               milestone: "First Trio",      value: 0,     expiryDays: 0,  totalStock: 9999, issued: 1823, active: true },
  { id: "4", name: "Circle K Drink Voucher", type: "VOUCHER",         partner: "Circle K Vietnam", milestone: "Summer Explorer", value: 35000, expiryDays: 45, totalStock: 300,  issued: 295, active: false },
];

const VOUCHERS = [
  { id: "1", code: "HC-2024-JUN-0001", milestone: "Week Warrior", status: "ASSIGNED",  userId: "usr_7f2a", assignedAt: "2024-06-12", expiresAt: "2024-07-12" },
  { id: "2", code: "HC-2024-JUN-0002", milestone: "Week Warrior", status: "AVAILABLE", userId: null,       assignedAt: null,          expiresAt: "2024-08-01" },
  { id: "3", code: "HC-2024-MAY-0003", milestone: "Week Warrior", status: "EXPIRED",   userId: "usr_3b9c", assignedAt: "2024-05-01",  expiresAt: "2024-05-31" },
  { id: "4", code: "GR-2024-JUN-0001", milestone: "Line Master",  status: "AVAILABLE", userId: null,       assignedAt: null,          expiresAt: "2024-09-01" },
  { id: "5", code: "GR-2024-JUN-0002", milestone: "Line Master",  status: "DISABLED",  userId: null,       assignedAt: null,          expiresAt: "2024-09-01" },
];

const ROLES = [
  { id: "1", name: "Super Admin",      description: "Full system access",              status: "ACTIVE", isSystem: true  },
  { id: "2", name: "Operator",         description: "Manage stations and campaigns",   status: "ACTIVE", isSystem: false },
  { id: "3", name: "Analyst",          description: "Read-only analytics access",      status: "ACTIVE", isSystem: false },
  { id: "4", name: "Partner Manager",  description: "Manage partner accounts",         status: "ACTIVE", isSystem: false },
];

const PERMISSIONS = [
  { id: "1", name: "stations:read",   description: "View station list and details" },
  { id: "2", name: "stations:write",  description: "Create and update stations" },
  { id: "3", name: "campaigns:read",  description: "View campaigns" },
  { id: "4", name: "campaigns:write", description: "Create and update campaigns" },
  { id: "5", name: "rewards:manage",  description: "Manage rewards and vouchers" },
  { id: "6", name: "rbac:manage",     description: "Manage roles and permissions" },
  { id: "7", name: "analytics:read",  description: "View analytics dashboards" },
];

const STAMPS_PER_CAMPAIGN = [
  { name: "Standard", stamps: 8231 },
  { name: "Summer 2024", stamps: 5840 },
  { name: "Tết 2024", stamps: 3204 },
  { name: "Christmas", stamps: 0 },
];

// ─── Derived helpers ──────────────────────────────────────────────────────────

const stationReady = (s: typeof STATIONS[0]) => s.gpsReady && s.scanKeyStatus === "CONFIGURED";
const notReadyStations = STATIONS.filter((s) => !stationReady(s));
const readyStations = STATIONS.filter(stationReady);

const daysUntil = (dateStr: string) => {
  const d = new Date(dateStr);
  return Math.ceil((d.getTime() - Date.now()) / 86400000);
};

const stockPct = (r: typeof REWARDS[0]) => r.totalStock > 0 ? ((r.totalStock - r.issued) / r.totalStock) * 100 : 0;
const lowStockRewards = REWARDS.filter((r) => r.active && stockPct(r) <= 20 && r.type === "VOUCHER");

// ─── Badge system ─────────────────────────────────────────────────────────────

const STATUS_CLS: Record<string, string> = {
  // operational states
  ACTIVE:      "bg-emerald-50 text-emerald-700 border-emerald-200",
  DRAFT:       "bg-[#F4F8FC] text-[#6B7280] border-[#E4E7EC]",
  INACTIVE:    "bg-orange-50 text-orange-600 border-orange-200",
  ARCHIVED:    "bg-slate-100 text-slate-500 border-slate-200",
  // readiness
  READY:       "bg-emerald-50 text-emerald-700 border-emerald-200",
  NOT_READY:   "bg-[#FDEDEB] text-[#E83B28] border-red-200",
  // GPS
  GPS_OK:      "bg-emerald-50 text-emerald-700 border-emerald-200",
  GPS_MISSING: "bg-[#FDEDEB] text-[#E83B28] border-red-200",
  // scan key
  SCAN_KEY_OK:      "bg-emerald-50 text-emerald-700 border-emerald-200",
  SCAN_KEY_MISSING: "bg-[#FDEDEB] text-[#E83B28] border-red-200",
  // voucher states
  AVAILABLE: "bg-emerald-50 text-emerald-700 border-emerald-200",
  ASSIGNED:  "bg-[#F4F8FC] text-[#01599D] border-blue-200",
  EXPIRED:   "bg-orange-50 text-orange-600 border-orange-200",
  DISABLED:  "bg-[#F4F8FC] text-[#6B7280] border-[#E4E7EC]",
  // stock
  IN_STOCK:    "bg-emerald-50 text-emerald-700 border-emerald-200",
  LOW_STOCK:   "bg-amber-50 text-amber-700 border-amber-200",
  OUT_OF_STOCK:"bg-[#FDEDEB] text-[#E83B28] border-red-200",
  // campaign type
  STANDARD: "bg-[#F4F8FC] text-[#01599D] border-blue-200",
  SEASONAL: "bg-amber-50 text-amber-700 border-amber-200",
  EVENT:    "bg-purple-50 text-purple-700 border-purple-200",
  // rarity
  COMMON:    "bg-[#F4F8FC] text-[#6B7280] border-[#E4E7EC]",
  RARE:      "bg-[#F4F8FC] text-[#01599D] border-blue-200",
  EPIC:      "bg-purple-50 text-purple-700 border-purple-200",
  LEGENDARY: "bg-amber-50 text-amber-700 border-amber-200",
  // reward type
  VOUCHER:         "bg-[#F4F8FC] text-[#01599D] border-blue-200",
  DIGITAL_STICKER: "bg-purple-50 text-purple-700 border-purple-200",
  BONUS_STAMP:     "bg-emerald-50 text-emerald-700 border-emerald-200",
  // contract
  CONTRACT_ACTIVE:  "bg-emerald-50 text-emerald-700 border-emerald-200",
  EXPIRING:         "bg-amber-50 text-amber-700 border-amber-200",
  EXPIRED_CONTRACT: "bg-[#FDEDEB] text-[#E83B28] border-red-200",
};

const STATUS_LABEL: Record<string, string> = {
  READY: "READY", NOT_READY: "NOT READY",
  GPS_OK: "GPS OK", GPS_MISSING: "GPS MISSING",
  SCAN_KEY_OK: "SCAN KEY OK", SCAN_KEY_MISSING: "SCAN KEY MISSING",
  IN_STOCK: "IN STOCK", LOW_STOCK: "LOW STOCK", OUT_OF_STOCK: "OUT OF STOCK",
  AVAILABLE: "AVAILABLE", ASSIGNED: "ASSIGNED", EXPIRED: "EXPIRED", DISABLED: "DISABLED",
  CONTRACT_ACTIVE: "ACTIVE", EXPIRING: "EXPIRING SOON", EXPIRED_CONTRACT: "EXPIRED",
  DIGITAL_STICKER: "DIGITAL STICKER", BONUS_STAMP: "BONUS STAMP",
};

function Badge({ status, label, dot }: { status: string; label?: string; dot?: boolean }) {
  const cls = STATUS_CLS[status] || "bg-[#F4F8FC] text-[#6B7280] border-[#E4E7EC]";
  const text = label ?? STATUS_LABEL[status] ?? status.replace(/_/g, " ");
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-semibold tracking-wide border whitespace-nowrap ${cls}`}>
      {dot && <span className={`w-1.5 h-1.5 rounded-full ${cls.includes("emerald") ? "bg-emerald-500" : cls.includes("amber") ? "bg-amber-500" : cls.includes("red") || cls.includes("E83B28") ? "bg-[#E83B28]" : "bg-[#6B7280]"}`} />}
      {text}
    </span>
  );
}

// readiness composite badge
function ReadinessBadge({ gpsReady, scanKeyConfigured }: { gpsReady: boolean; scanKeyConfigured: boolean }) {
  const ready = gpsReady && scanKeyConfigured;
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-semibold tracking-wide border ${ready ? STATUS_CLS.READY : STATUS_CLS.NOT_READY}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${ready ? "bg-emerald-500" : "bg-[#E83B28]"}`} />
      {ready ? "READY" : "NOT READY"}
    </span>
  );
}

// stock badge
function StockBadge({ total, issued }: { total: number; issued: number }) {
  const remaining = total - issued;
  const pct = total > 0 ? (remaining / total) * 100 : 0;
  if (remaining === 0) return <Badge status="OUT_OF_STOCK" dot />;
  if (pct <= 20)       return <Badge status="LOW_STOCK" dot />;
  return <Badge status="IN_STOCK" dot />;
}

// ─── Primitive components ─────────────────────────────────────────────────────

function Btn({ children, onClick, variant = "primary", size = "sm", className = "", disabled = false }: {
  children: React.ReactNode; onClick?: () => void;
  variant?: "primary" | "secondary" | "ghost" | "danger" | "outline";
  size?: "sm" | "md"; className?: string; disabled?: boolean;
}) {
  const base = "inline-flex items-center gap-1.5 font-medium rounded transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 disabled:opacity-50 disabled:cursor-not-allowed";
  const sizes = { sm: "px-3 py-1.5 text-sm", md: "px-4 py-2 text-sm" };
  const variants = {
    primary:   "bg-[#01599D] text-white hover:bg-[#014d8a] focus:ring-[#01599D]",
    secondary: "bg-white text-[#1D2433] hover:bg-[#F4F8FC] border border-[#E4E7EC] focus:ring-[#01599D]",
    ghost:     "text-[#6B7280] hover:bg-[#F4F8FC] focus:ring-[#01599D]",
    danger:    "bg-[#E83B28] text-white hover:bg-[#c92f1f] focus:ring-[#E83B28]",
    outline:   "border border-[#E4E7EC] text-[#1D2433] hover:bg-[#F4F8FC] focus:ring-[#01599D]",
  };
  return (
    <button onClick={onClick} disabled={disabled} className={`${base} ${sizes[size]} ${variants[variant]} ${className}`}>
      {children}
    </button>
  );
}

function Input({ label, placeholder, value, onChange, type = "text", required = false, mono = false, hint }: {
  label?: string; placeholder?: string; value: string; onChange: (v: string) => void;
  type?: string; required?: boolean; mono?: boolean; hint?: string;
}) {
  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label className="text-xs font-semibold text-[#6B7280] uppercase tracking-wide">
          {label}{required && <span className="text-[#E83B28] ml-0.5">*</span>}
        </label>
      )}
      <input
        type={type} value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder}
        className={`px-3 py-2 text-sm bg-white border border-[#E4E7EC] rounded focus:outline-none focus:ring-2 focus:ring-[#01599D] focus:border-transparent text-[#1D2433] ${mono ? "font-mono" : ""}`}
      />
      {hint && <p className="text-[11px] text-[#6B7280]">{hint}</p>}
    </div>
  );
}

function SelectField({ label, value, onChange, options, required = false }: {
  label?: string; value: string; onChange: (v: string) => void;
  options: { label: string; value: string }[]; required?: boolean;
}) {
  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label className="text-xs font-semibold text-[#6B7280] uppercase tracking-wide">
          {label}{required && <span className="text-[#E83B28] ml-0.5">*</span>}
        </label>
      )}
      <select value={value} onChange={(e) => onChange(e.target.value)}
        className="px-3 py-2 text-sm bg-white border border-[#E4E7EC] rounded focus:outline-none focus:ring-2 focus:ring-[#01599D] text-[#1D2433] appearance-none">
        {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
    </div>
  );
}

function DrawerSection({ title }: { title: string }) {
  return <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest pt-2 border-t border-[#E4E7EC] first:border-0 first:pt-0">{title}</p>;
}

function Card({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return <div className={`bg-white border border-[#E4E7EC] rounded-lg ${className}`}>{children}</div>;
}

function KV({ label, value, mono = false }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div className="flex items-start justify-between gap-4 py-2 border-b border-[#E4E7EC] last:border-0 text-sm">
      <span className="text-[#6B7280] shrink-0">{label}</span>
      <span className={`text-[#1D2433] font-medium text-right ${mono ? "font-mono text-xs" : ""}`}>{value}</span>
    </div>
  );
}

function AuditRow({ updatedAt, updatedBy }: { updatedAt: string; updatedBy: string }) {
  return (
    <div className="flex items-center gap-3 pt-2 text-[11px] text-[#6B7280]">
      <Clock size={11} />
      <span>Updated {updatedAt} by <span className="font-mono">{updatedBy}</span></span>
    </div>
  );
}

function TableWrapper({ children }: { children: React.ReactNode }) {
  return <div className="overflow-x-auto"><table className="w-full text-sm text-left border-collapse">{children}</table></div>;
}

function Th({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return (
    <th className={`px-4 py-2.5 text-[10px] font-bold text-[#6B7280] uppercase tracking-widest bg-[#F4F8FC] border-b border-[#E4E7EC] whitespace-nowrap ${className}`}>
      {children}
    </th>
  );
}

function Td({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return <td className={`px-4 py-2.5 text-sm text-[#1D2433] border-b border-[#E4E7EC] ${className}`}>{children}</td>;
}

function MonoCode({ children }: { children: React.ReactNode }) {
  return <code className="font-mono text-xs bg-[#F4F8FC] px-1.5 py-0.5 rounded border border-[#E4E7EC] text-[#1D2433]">{children}</code>;
}

function Breadcrumb({ items }: { items: string[] }) {
  return (
    <div className="flex items-center gap-1 text-xs text-[#6B7280] mb-4 font-medium">
      {items.map((item, i) => (
        <span key={i} className="flex items-center gap-1">
          {i > 0 && <ChevronRight size={12} />}
          <span className={i === items.length - 1 ? "text-[#1D2433]" : ""}>{item}</span>
        </span>
      ))}
    </div>
  );
}

function EmptyState({ message = "No records found", icon: Icon = AlertCircle }: { message?: string; icon?: React.ElementType }) {
  return (
    <div className="flex flex-col items-center justify-center py-14 text-[#6B7280]">
      <Icon size={28} className="mb-3 opacity-25" />
      <p className="text-sm">{message}</p>
    </div>
  );
}

function Pagination({ shown, total }: { shown: number; total: number }) {
  return (
    <div className="flex items-center justify-between px-4 py-3 border-t border-[#E4E7EC]">
      <p className="text-xs text-[#6B7280]">Showing <span className="font-medium text-[#1D2433]">{shown}</span> of <span className="font-medium text-[#1D2433]">{total}</span> records</p>
      <div className="flex items-center gap-1">
        <Btn variant="ghost" size="sm"><ChevronLeft size={13} /></Btn>
        <span className="px-2.5 py-1 text-xs bg-[#01599D] text-white rounded font-medium">1</span>
        <Btn variant="ghost" size="sm"><ChevronRight size={13} /></Btn>
      </div>
    </div>
  );
}

function FilterBar({ children }: { children: React.ReactNode }) {
  return <div className="flex items-center gap-2 px-4 py-3 border-b border-[#E4E7EC] flex-wrap">{children}</div>;
}

function SearchInput({ value, onChange, placeholder }: { value: string; onChange: (v: string) => void; placeholder?: string }) {
  return (
    <div className="relative flex-1 min-w-[160px] max-w-xs">
      <Search size={13} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#6B7280]" />
      <input value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder ?? "Search…"}
        className="w-full pl-8 pr-3 py-1.5 text-sm bg-white border border-[#E4E7EC] rounded focus:outline-none focus:ring-2 focus:ring-[#01599D]" />
    </div>
  );
}

function FilterSelect({ value, onChange, options }: { value: string; onChange: (v: string) => void; options: [string, string][] }) {
  return (
    <select value={value} onChange={(e) => onChange(e.target.value)}
      className="px-3 py-1.5 text-sm bg-white border border-[#E4E7EC] rounded focus:outline-none text-[#1D2433]">
      {options.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
    </select>
  );
}

function MaskedValue({ value, label }: { value: string; label?: string }) {
  const [revealed, setRevealed] = useState(false);
  const [copied, setCopied] = useState(false);
  const handleCopy = () => { navigator.clipboard.writeText(value).catch(() => {}); setCopied(true); setTimeout(() => setCopied(false), 1500); };
  return (
    <div className="space-y-1">
      {label && <p className="text-xs text-[#6B7280] font-medium">{label}</p>}
      <div className="flex items-center gap-2">
        <code className="font-mono text-xs bg-[#F4F8FC] px-2 py-1.5 rounded border border-[#E4E7EC] text-[#1D2433] tracking-wider">
          {revealed ? value : "••••  ••••  ••••  ••••"}
        </code>
        <button onClick={() => setRevealed(!revealed)} title={revealed ? "Hide" : "Reveal"} className="p-1 rounded hover:bg-[#F4F8FC] text-[#6B7280] hover:text-[#01599D] transition-colors border border-transparent hover:border-[#E4E7EC]">
          {revealed ? <EyeOff size={13} /> : <Eye size={13} />}
        </button>
        <button onClick={handleCopy} title="Copy" className="p-1 rounded hover:bg-[#F4F8FC] text-[#6B7280] hover:text-[#01599D] transition-colors border border-transparent hover:border-[#E4E7EC]">
          {copied ? <Check size={13} className="text-emerald-600" /> : <Copy size={13} />}
        </button>
      </div>
    </div>
  );
}

// ─── Alert banner ─────────────────────────────────────────────────────────────

function AlertBanner({ type, children }: { type: "warn" | "error" | "info"; children: React.ReactNode }) {
  const cfg = {
    warn:  { cls: "bg-amber-50 border-amber-200 text-amber-800", Icon: TriangleAlert },
    error: { cls: "bg-[#FDEDEB] border-red-200 text-[#E83B28]", Icon: CircleAlert },
    info:  { cls: "bg-[#F4F8FC] border-[#E4E7EC] text-[#01599D]", Icon: Info },
  }[type];
  return (
    <div className={`flex items-start gap-3 px-4 py-3 rounded-lg border text-sm mb-4 ${cfg.cls}`}>
      <cfg.Icon size={15} className="shrink-0 mt-0.5" />
      <span>{children}</span>
    </div>
  );
}

// ─── Drawer ───────────────────────────────────────────────────────────────────

function Drawer({ open, onClose, title, subtitle, children }: {
  open: boolean; onClose: () => void; title: string; subtitle?: string; children: React.ReactNode;
}) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex">
      <div className="flex-1 bg-black/30 backdrop-blur-sm" onClick={onClose} />
      <div className="w-[480px] bg-white border-l border-[#E4E7EC] flex flex-col h-full shadow-2xl">
        <div className="flex items-start justify-between px-6 py-4 border-b border-[#E4E7EC] bg-[#F4F8FC]">
          <div>
            <h2 className="text-sm font-semibold text-[#1D2433]">{title}</h2>
            {subtitle && <p className="text-xs text-[#6B7280] mt-0.5">{subtitle}</p>}
          </div>
          <button onClick={onClose} className="text-[#6B7280] hover:text-[#1D2433] mt-0.5"><X size={16} /></button>
        </div>
        <div className="flex-1 overflow-y-auto px-6 py-5 space-y-4">{children}</div>
        <div className="px-6 py-4 border-t border-[#E4E7EC] bg-[#F4F8FC] flex items-center gap-3">
          <Btn variant="primary" size="md" onClick={onClose}>Save Changes</Btn>
          <Btn variant="outline" size="md" onClick={onClose}>Cancel</Btn>
          <span className="ml-auto text-[11px] text-[#6B7280]">Unsaved changes will be lost</span>
        </div>
      </div>
    </div>
  );
}

// ─── Confirm Modal ────────────────────────────────────────────────────────────

function ConfirmModal({ open, onClose, onConfirm, title, message, confirmLabel = "Confirm", dangerous = false }: {
  open: boolean; onClose: () => void; onConfirm: () => void;
  title: string; message: string; confirmLabel?: string; dangerous?: boolean;
}) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white rounded-lg border border-[#E4E7EC] shadow-2xl w-full max-w-md p-6">
        <div className={`flex items-center gap-2 mb-1 ${dangerous ? "text-[#E83B28]" : "text-[#01599D]"}`}>
          <AlertTriangle size={17} />
          <h3 className="text-sm font-semibold text-[#1D2433]">{title}</h3>
        </div>
        <p className="text-sm text-[#6B7280] mb-6 mt-2 leading-relaxed">{message}</p>
        <div className="flex gap-3">
          <Btn variant={dangerous ? "danger" : "primary"} size="md" onClick={() => { onConfirm(); onClose(); }}>{confirmLabel}</Btn>
          <Btn variant="outline" size="md" onClick={onClose}>Cancel</Btn>
        </div>
      </div>
    </div>
  );
}

// ─── Asset Upload Modal ───────────────────────────────────────────────────────

function AssetUploadModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [state, setState] = useState<"idle" | "uploading" | "success" | "failed">("idle");
  const [dragging, setDragging] = useState(false);
  const fakeUrl = "https://cdn.exoticstamp.vn/assets/station-img-bt-001.jpg";
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white rounded-lg border border-[#E4E7EC] shadow-2xl w-full max-w-lg p-6">
        <div className="flex items-center justify-between mb-5">
          <div>
            <h3 className="text-sm font-semibold text-[#1D2433]">Upload Asset</h3>
            <p className="text-xs text-[#6B7280] mt-0.5">PNG, JPG, SVG — max 5 MB</p>
          </div>
          <button onClick={onClose} className="text-[#6B7280] hover:text-[#1D2433]"><X size={16} /></button>
        </div>
        {state === "idle" && (
          <div
            onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
            onDragLeave={() => setDragging(false)}
            onDrop={(e) => { e.preventDefault(); setDragging(false); setState("uploading"); setTimeout(() => setState("success"), 1200); }}
            className={`border-2 border-dashed rounded-lg p-10 text-center cursor-pointer transition-colors ${dragging ? "border-[#01599D] bg-[#F4F8FC]" : "border-[#E4E7EC] hover:border-[#01599D] hover:bg-[#F4F8FC]"}`}
            onClick={() => { setState("uploading"); setTimeout(() => setState("success"), 1200); }}
          >
            <Upload size={28} className="mx-auto mb-3 text-[#6B7280]" />
            <p className="text-sm font-medium text-[#1D2433]">Drag & drop or click to select file</p>
          </div>
        )}
        {state === "uploading" && (
          <div className="flex flex-col items-center py-10 gap-3">
            <div className="w-9 h-9 border-4 border-[#01599D] border-t-transparent rounded-full animate-spin" />
            <p className="text-sm text-[#6B7280]">Uploading to CDN…</p>
          </div>
        )}
        {state === "success" && (
          <div className="space-y-4">
            <div className="flex items-center gap-2 text-emerald-700 text-sm font-medium bg-emerald-50 px-3 py-2 rounded border border-emerald-200">
              <CheckCircle2 size={14} /> Upload successful
            </div>
            <div className="bg-[#F4F8FC] rounded border border-[#E4E7EC] aspect-video overflow-hidden">
              <img src="https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=480&h=270&fit=crop&auto=format" alt="Uploaded preview" className="w-full h-full object-cover" />
            </div>
            <div className="space-y-1">
              <p className="text-xs font-semibold text-[#6B7280] uppercase tracking-wide">Public URL</p>
              <div className="flex items-center gap-2">
                <code className="text-xs font-mono bg-[#F4F8FC] border border-[#E4E7EC] px-2 py-1.5 rounded flex-1 truncate">{fakeUrl}</code>
                <Btn variant="outline" size="sm" onClick={() => {}}><Copy size={12} /> Copy</Btn>
              </div>
            </div>
            <Btn variant="primary" size="md" onClick={onClose} className="w-full justify-center">Use this URL in current form</Btn>
          </div>
        )}
        {state === "failed" && (
          <div className="flex flex-col items-center py-10 gap-3">
            <XCircle size={28} className="text-[#E83B28]" />
            <p className="text-sm text-[#E83B28] font-medium">Upload failed — server error</p>
            <Btn variant="secondary" size="sm" onClick={() => setState("idle")}>Try again</Btn>
          </div>
        )}
      </div>
    </div>
  );
}

// ─── Login ────────────────────────────────────────────────────────────────────

function LoginScreen({ onLogin }: { onLogin: () => void }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = () => {
    if (!email || !password) { setError("Email and password are required."); return; }
    setLoading(true); setError("");
    setTimeout(() => { setLoading(false); onLogin(); }, 900);
  };

  return (
    <div className="min-h-screen bg-[#F4F8FC] flex flex-col items-center justify-center">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-12 h-12 bg-[#01599D] rounded-xl mb-4 shadow-sm">
            <Train size={24} className="text-white" />
          </div>
          <h1 className="text-lg font-semibold text-[#1D2433]">Exotic Stamp Admin</h1>
          <p className="text-sm text-[#6B7280] mt-1">Metro Stamp Operations Console</p>
        </div>
        <Card className="p-6 shadow-sm">
          <div className="space-y-4">
            <Input label="Email" placeholder="admin@exoticstamp.vn" value={email} onChange={setEmail} type="email" required />
            <Input label="Password" placeholder="••••••••" value={password} onChange={setPassword} type="password" required />
            {error && <AlertBanner type="error">{error}</AlertBanner>}
            <button onClick={handleSubmit} disabled={loading}
              className="w-full py-2.5 bg-[#01599D] text-white text-sm font-semibold rounded-lg hover:bg-[#014d8a] transition-colors disabled:opacity-60 flex items-center justify-center gap-2">
              {loading ? <><div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />Signing in…</> : "Sign In"}
            </button>
          </div>
        </Card>
        <p className="text-center text-[11px] text-[#6B7280] mt-5">Internal admin console — authorized personnel only</p>
      </div>
    </div>
  );
}

// ─── Sidebar ──────────────────────────────────────────────────────────────────

const NAV_ITEMS = [
  { id: "dashboard",     label: "Dashboard",        icon: LayoutDashboard },
  { id: "metro-lines",   label: "Metro Lines",       icon: Train           },
  { id: "stations",      label: "Stations",          icon: MapPin          },
  { id: "campaigns",     label: "Campaigns",         icon: Megaphone       },
  { id: "stamp-designs", label: "Stamp Designs",     icon: Stamp           },
  { id: "partners",      label: "Partners",          icon: Handshake       },
  { id: "milestones",    label: "Milestones",        icon: Trophy          },
  { id: "rewards",       label: "Rewards & Vouchers",icon: Gift            },
  { id: "analytics",     label: "Analytics",         icon: BarChart2       },
  { id: "rbac",          label: "RBAC",              icon: ShieldCheck     },
  { id: "settings",      label: "Settings",          icon: Settings        },
];

// Issue indicators per section
const NAV_ISSUES: Record<string, number> = {
  stations: notReadyStations.length,
  rewards: lowStockRewards.length,
};

function Sidebar({ current, onNavigate, collapsed, onToggle }: {
  current: Screen; onNavigate: (s: Screen) => void; collapsed: boolean; onToggle: () => void;
}) {
  return (
    <aside className={`flex flex-col bg-white border-r border-[#E4E7EC] transition-all duration-200 ${collapsed ? "w-16" : "w-60"} shrink-0 h-full`}>
      <div className="flex items-center justify-between px-3 py-3 border-b border-[#E4E7EC] h-14">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-[#01599D] rounded-lg flex items-center justify-center shrink-0">
              <Train size={16} className="text-white" />
            </div>
            <div className="leading-tight">
              <p className="text-xs font-bold text-[#1D2433]">Exotic Stamp</p>
              <p className="text-[10px] text-[#6B7280] font-medium uppercase tracking-wide">Admin Console</p>
            </div>
          </div>
        )}
        <button onClick={onToggle} className="text-[#6B7280] hover:text-[#01599D] transition-colors p-1 rounded">
          {collapsed ? <ChevronRight size={15} /> : <ChevronLeft size={15} />}
        </button>
      </div>
      <nav className="flex-1 overflow-y-auto py-2 space-y-0.5 px-2">
        {NAV_ITEMS.map(({ id, label, icon: Icon }) => {
          const isActive = current === id
            || (current === "metro-line-detail" && id === "metro-lines")
            || (current === "station-detail"    && id === "stations")
            || (current === "campaign-detail"   && id === "campaigns");
          const issues = NAV_ISSUES[id] ?? 0;
          return (
            <button key={id} onClick={() => onNavigate(id as Screen)}
              className={`w-full flex items-center gap-2.5 px-2.5 py-2 rounded text-sm transition-colors ${isActive ? "bg-[#F4F8FC] text-[#01599D] font-semibold" : "text-[#6B7280] hover:bg-[#F4F8FC] hover:text-[#1D2433]"}`}>
              <Icon size={15} className="shrink-0" />
              {!collapsed && <span className="flex-1 text-left">{label}</span>}
              {!collapsed && issues > 0 && (
                <span className="text-[10px] font-bold bg-[#E83B28] text-white px-1.5 py-0.5 rounded-full leading-none">{issues}</span>
              )}
              {collapsed && issues > 0 && (
                <span className="absolute right-1 top-1 w-2 h-2 bg-[#E83B28] rounded-full" />
              )}
            </button>
          );
        })}
      </nav>
      {!collapsed && (
        <div className="px-4 py-2.5 border-t border-[#E4E7EC]">
          <p className="text-[10px] text-[#6B7280] font-mono">v1.0.0-beta · STAGING</p>
        </div>
      )}
    </aside>
  );
}

// ─── Header ───────────────────────────────────────────────────────────────────

function Header({ title, onLogout }: { title: string; onLogout: () => void }) {
  return (
    <header className="h-14 bg-white border-b border-[#E4E7EC] flex items-center justify-between px-6 shrink-0">
      <div className="flex items-center gap-3">
        <h1 className="text-sm font-semibold text-[#1D2433]">{title}</h1>
        <span className="px-2 py-0.5 text-[10px] font-bold bg-amber-50 text-amber-700 border border-amber-200 rounded uppercase tracking-wide">Staging</span>
      </div>
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-1.5 text-[11px] text-[#6B7280] bg-[#F4F8FC] border border-[#E4E7EC] px-2.5 py-1 rounded">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
          API Healthy · Last sync 14:32
        </div>
        <button className="text-[#6B7280] hover:text-[#01599D] transition-colors relative">
          <Bell size={17} />
          {notReadyStations.length > 0 && <span className="absolute -top-1 -right-1 w-3.5 h-3.5 bg-[#E83B28] rounded-full text-white text-[8px] font-bold flex items-center justify-center">{notReadyStations.length}</span>}
        </button>
        <div className="flex items-center gap-2 pl-3 border-l border-[#E4E7EC]">
          <div className="w-7 h-7 rounded-full bg-[#01599D] flex items-center justify-center text-white text-[11px] font-bold">NA</div>
          <div className="leading-tight">
            <p className="text-xs font-semibold text-[#1D2433]">Nguyen Admin</p>
            <p className="text-[10px] text-[#6B7280]">Super Admin</p>
          </div>
        </div>
        <button onClick={onLogout} className="p-1.5 rounded hover:bg-[#FDEDEB] text-[#6B7280] hover:text-[#E83B28] transition-colors">
          <LogOut size={15} />
        </button>
      </div>
    </header>
  );
}

// ─── Dashboard ────────────────────────────────────────────────────────────────

function DashboardScreen({ onNavigate }: { onNavigate: (s: Screen) => void }) {
  return (
    <div className="space-y-5">
      {/* Operational summary strip */}
      <div className="grid grid-cols-4 gap-3">
        {[
          { label: "Line 1 Station Readiness", value: `${readyStations.length} / ${STATIONS.length}`, sub: `${notReadyStations.length} stations not ready`, ok: notReadyStations.length === 0, icon: MapPin },
          { label: "Active Campaigns", value: `${CAMPAIGNS.filter(c => c.status === "ACTIVE").length} running`, sub: "1 draft pending activation", ok: true, icon: Megaphone },
          { label: "Voucher Pool Health", value: "413 available", sub: `${lowStockRewards.length} reward${lowStockRewards.length !== 1 ? "s" : ""} low stock`, ok: lowStockRewards.length === 0, icon: Ticket },
          { label: "Total Stamps Collected", value: "17,445", sub: "+342 in last 7 days", ok: true, icon: Stamp },
        ].map(({ label, value, sub, ok, icon: Icon }) => (
          <Card key={label} className={`p-4 border-l-4 ${ok ? "border-l-emerald-500" : "border-l-[#E83B28]"}`}>
            <div className="flex items-start justify-between mb-2">
              <p className="text-[11px] font-semibold text-[#6B7280] uppercase tracking-wide">{label}</p>
              <Icon size={15} className={ok ? "text-emerald-600" : "text-[#E83B28]"} />
            </div>
            <p className="text-xl font-bold text-[#1D2433] leading-none mb-1">{value}</p>
            <p className={`text-[11px] ${ok ? "text-[#6B7280]" : "text-[#E83B28] font-medium"}`}>{sub}</p>
          </Card>
        ))}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-2 gap-4">
        <Card className="p-5">
          <p className="text-xs font-bold text-[#6B7280] uppercase tracking-widest mb-4">Stamps per Campaign</p>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={STAMPS_PER_CAMPAIGN} barSize={28}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E4E7EC" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 11, fill: "#6B7280" }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 11, fill: "#6B7280" }} axisLine={false} tickLine={false} />
              <Tooltip contentStyle={{ fontSize: 12, border: "1px solid #E4E7EC", borderRadius: 6 }} />
              <Bar dataKey="stamps" fill="#01599D" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>
        <Card className="p-5">
          <p className="text-xs font-bold text-[#6B7280] uppercase tracking-widest mb-4">Top Stations by Collector Count</p>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={STATIONS.slice(0, 5).map(s => ({ name: s.name.split(" ").pop(), collectors: s.collectors }))} layout="vertical" barSize={14}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E4E7EC" horizontal={false} />
              <XAxis type="number" tick={{ fontSize: 11, fill: "#6B7280" }} axisLine={false} tickLine={false} />
              <YAxis type="category" dataKey="name" tick={{ fontSize: 11, fill: "#6B7280" }} axisLine={false} tickLine={false} width={80} />
              <Tooltip contentStyle={{ fontSize: 12, border: "1px solid #E4E7EC", borderRadius: 6 }} />
              <Bar dataKey="collectors" fill="#009B3A" radius={[0, 3, 3, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>
      </div>

      {/* Operational warnings + quick actions */}
      <div className="grid grid-cols-3 gap-4">
        <Card className="col-span-2">
          <div className="flex items-center justify-between px-4 py-3 border-b border-[#E4E7EC]">
            <p className="text-xs font-bold text-[#6B7280] uppercase tracking-widest">Station Readiness Warnings</p>
            <Badge status="NOT_READY" label={`${notReadyStations.length} not ready`} dot />
          </div>
          <div className="divide-y divide-[#E4E7EC]">
            {notReadyStations.map((s) => (
              <div key={s.id} className="flex items-center justify-between px-4 py-3">
                <div className="flex items-center gap-3">
                  <AlertTriangle size={13} className="text-[#E83B28] shrink-0" />
                  <div>
                    <p className="text-sm font-semibold text-[#1D2433]">{s.name}</p>
                    <p className="text-[11px] text-[#6B7280] font-mono">{s.code} · {s.address}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  {!s.gpsReady && <Badge status="GPS_MISSING" dot />}
                  {s.scanKeyStatus === "MISSING" && <Badge status="SCAN_KEY_MISSING" dot />}
                  <button onClick={() => onNavigate("station-detail")} className="text-[11px] text-[#01599D] font-semibold hover:underline ml-1">Fix →</button>
                </div>
              </div>
            ))}
          </div>
        </Card>
        <Card className="p-4">
          <p className="text-xs font-bold text-[#6B7280] uppercase tracking-widest mb-3">Quick Actions</p>
          <div className="space-y-2">
            {[
              { label: "Add Station",     note: "Configure GPS + scan key",  screen: "stations"  as Screen, icon: MapPin     },
              { label: "Create Campaign", note: "Set dates and assign stations", screen: "campaigns" as Screen, icon: Megaphone  },
              { label: "Import Vouchers", note: "Bulk upload voucher codes",  screen: "rewards"   as Screen, icon: Download   },
            ].map(({ label, note, screen, icon: Icon }) => (
              <button key={label} onClick={() => onNavigate(screen)}
                className="w-full flex items-center justify-between px-3 py-2.5 rounded border border-[#E4E7EC] hover:bg-[#F4F8FC] hover:border-[#01599D] transition-colors text-left group">
                <span className="flex items-start gap-2.5">
                  <Icon size={14} className="text-[#6B7280] group-hover:text-[#01599D] mt-0.5 shrink-0" />
                  <span>
                    <p className="text-sm font-semibold text-[#1D2433]">{label}</p>
                    <p className="text-[11px] text-[#6B7280]">{note}</p>
                  </span>
                </span>
                <ArrowRight size={13} className="text-[#6B7280] shrink-0" />
              </button>
            ))}
          </div>
          {lowStockRewards.length > 0 && (
            <div className="mt-3 pt-3 border-t border-[#E4E7EC]">
              <p className="text-[11px] font-semibold text-[#E83B28] flex items-center gap-1 mb-1">
                <AlertTriangle size={11} /> {lowStockRewards.length} reward{lowStockRewards.length > 1 ? "s" : ""} low on stock
              </p>
              {lowStockRewards.map(r => (
                <p key={r.id} className="text-[11px] text-[#6B7280]">· {r.name} ({r.totalStock - r.issued} remaining)</p>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}

// ─── Metro Lines ──────────────────────────────────────────────────────────────

function MetroLinesScreen({ onViewDetail }: { onViewDetail: () => void }) {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null);
  const [draftCode, setDraftCode] = useState("");
  const [draftName, setDraftName] = useState("");
  const [draftStatus, setDraftStatus] = useState("DRAFT");

  const filtered = METRO_LINES.filter((l) =>
    (statusFilter === "ALL" || l.status === statusFilter) &&
    (l.name.toLowerCase().includes(search.toLowerCase()) || l.code.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <p className="text-xs text-[#6B7280]"><span className="font-semibold text-[#1D2433]">{METRO_LINES.length}</span> lines configured</p>
        <Btn variant="primary" size="sm" onClick={() => setDrawerOpen(true)}><Plus size={14} /> Create Line</Btn>
      </div>
      <Card>
        <FilterBar>
          <SearchInput value={search} onChange={setSearch} placeholder="Search by code or name…" />
          <FilterSelect value={statusFilter} onChange={setStatusFilter} options={[["ALL","All Status"],["ACTIVE","Active"],["DRAFT","Draft"],["INACTIVE","Inactive"]]} />
        </FilterBar>
        <TableWrapper>
          <thead><tr>
            <Th>Code</Th><Th>Name</Th><Th>Display Name</Th><Th>Color</Th>
            <Th>Stations</Th><Th>Readiness</Th><Th>Status</Th><Th>Sort</Th><Th>Last Updated</Th><Th>Actions</Th>
          </tr></thead>
          <tbody>
            {filtered.length === 0 ? <tr><td colSpan={10}><EmptyState message="No metro lines match your search" icon={Train} /></td></tr>
            : filtered.map((line) => (
              <tr key={line.id} className="hover:bg-[#F4F8FC] transition-colors">
                <Td><MonoCode>{line.code}</MonoCode></Td>
                <Td className="font-semibold">{line.name}</Td>
                <Td className="text-[#6B7280]">{line.displayName}</Td>
                <Td>
                  <div className="flex items-center gap-2">
                    <div className="w-5 h-5 rounded border border-[#E4E7EC]" style={{ backgroundColor: line.color }} />
                    <MonoCode>{line.color}</MonoCode>
                  </div>
                </Td>
                <Td className="font-mono text-xs">{line.stations}</Td>
                <Td>
                  {line.stations > 0 ? (
                    <div className="flex items-center gap-2">
                      <div className="w-20 h-1.5 bg-[#E4E7EC] rounded-full overflow-hidden">
                        <div className="h-full bg-emerald-500 rounded-full" style={{ width: `${(line.readyStations / line.stations) * 100}%` }} />
                      </div>
                      <span className="text-xs text-[#6B7280] font-mono">{line.readyStations}/{line.stations}</span>
                    </div>
                  ) : <span className="text-xs text-[#6B7280]">—</span>}
                </Td>
                <Td><Badge status={line.status} dot /></Td>
                <Td className="font-mono text-xs">{line.sortOrder}</Td>
                <Td>
                  <p className="text-xs text-[#6B7280]">{line.updatedAt}</p>
                  <p className="text-[10px] text-[#6B7280] font-mono">{line.updatedBy}</p>
                </Td>
                <Td>
                  <div className="flex items-center gap-1">
                    <button onClick={onViewDetail} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Eye size={13} /></button>
                    <button onClick={() => setDrawerOpen(true)} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Pencil size={13} /></button>
                    <button onClick={() => setDeleteConfirm(line.code)} className="p-1 rounded hover:bg-[#FDEDEB] text-[#6B7280] hover:text-[#E83B28]"><Trash2 size={13} /></button>
                  </div>
                </Td>
              </tr>
            ))}
          </tbody>
        </TableWrapper>
        <Pagination shown={filtered.length} total={METRO_LINES.length} />
      </Card>
      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Create Metro Line" subtitle="New line will be created in DRAFT status">
        <DrawerSection title="Identity" />
        <Input label="Code" placeholder="ML2" value={draftCode} onChange={setDraftCode} required mono hint="Short uppercase identifier, e.g. ML1, ML2, ML3A" />
        <Input label="Name" placeholder="Metro Line 2" value={draftName} onChange={setDraftName} required />
        <Input label="Display Name (Vietnamese)" placeholder="Tuyến số 2" value="" onChange={() => {}} />
        <Input label="Description" placeholder="Route description…" value="" onChange={() => {}} />
        <DrawerSection title="Visual" />
        <div className="flex flex-col gap-1">
          <label className="text-xs font-semibold text-[#6B7280] uppercase tracking-wide">Line Color</label>
          <div className="flex items-center gap-3">
            <input type="color" defaultValue="#01599D" className="w-10 h-10 rounded cursor-pointer border border-[#E4E7EC]" />
            <Input label="" placeholder="#01599D" value="" onChange={() => {}} mono />
          </div>
        </div>
        <Input label="Sort Order" placeholder="2" value="" onChange={() => {}} type="number" />
        <DrawerSection title="Status" />
        <SelectField label="Status" value={draftStatus} onChange={setDraftStatus} options={[{label:"Draft",value:"DRAFT"},{label:"Active",value:"ACTIVE"},{label:"Inactive",value:"INACTIVE"}]} />
      </Drawer>
      <ConfirmModal open={!!deleteConfirm} onClose={() => setDeleteConfirm(null)} onConfirm={() => {}} title={`Soft-delete Line ${deleteConfirm}`}
        message={`This will hide Metro Line "${deleteConfirm}" from the mobile app. All stations assigned to this line remain intact and can be reassigned. This action can be reversed by an admin.`}
        confirmLabel="Delete Line" dangerous />
    </div>
  );
}

// ─── Stations ─────────────────────────────────────────────────────────────────

function StationsScreen({ onViewDetail }: { onViewDetail: () => void }) {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [readinessFilter, setReadinessFilter] = useState("ALL");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [assetUploadOpen, setAssetUploadOpen] = useState(false);

  const filtered = STATIONS.filter((s) => {
    const matchSearch = s.name.toLowerCase().includes(search.toLowerCase()) || s.code.toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter === "ALL" || s.status === statusFilter;
    const ready = stationReady(s);
    const matchReadiness = readinessFilter === "ALL"
      || (readinessFilter === "READY" && ready)
      || (readinessFilter === "NOT_READY" && !ready)
      || (readinessFilter === "GPS_MISSING" && !s.gpsReady)
      || (readinessFilter === "KEY_MISSING" && s.scanKeyStatus === "MISSING");
    return matchSearch && matchStatus && matchReadiness;
  });

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <p className="text-xs text-[#6B7280]"><span className="font-semibold text-[#1D2433]">{STATIONS.length}</span> stations · <span className="font-semibold text-emerald-700">{readyStations.length} ready</span> · <span className="font-semibold text-[#E83B28]">{notReadyStations.length} not ready</span></p>
        </div>
        <Btn variant="primary" size="sm" onClick={() => setDrawerOpen(true)}><Plus size={14} /> Add Station</Btn>
      </div>
      {notReadyStations.length > 0 && (
        <AlertBanner type="error">
          <strong>{notReadyStations.length} station{notReadyStations.length > 1 ? "s" : ""} cannot accept scans</strong> — missing GPS coordinates or scan keys. Resolve before activating campaigns on these stations.
        </AlertBanner>
      )}
      <Card>
        <FilterBar>
          <SearchInput value={search} onChange={setSearch} placeholder="Search station code or name…" />
          <FilterSelect value={statusFilter} onChange={setStatusFilter} options={[["ALL","All Status"],["ACTIVE","Active"],["DRAFT","Draft"],["INACTIVE","Inactive"]]} />
          <FilterSelect value={readinessFilter} onChange={setReadinessFilter} options={[["ALL","All Readiness"],["READY","Ready only"],["NOT_READY","Not Ready"],["GPS_MISSING","GPS Missing"],["KEY_MISSING","Scan Key Missing"]]} />
        </FilterBar>
        <TableWrapper>
          <thead><tr>
            <Th>Code</Th><Th>Station Name</Th><Th>Line</Th><Th>Status</Th>
            <Th>Readiness</Th><Th>GPS</Th><Th>Scan Key</Th>
            <Th>Collectors</Th><Th>Last Scan</Th><Th>Actions</Th>
          </tr></thead>
          <tbody>
            {filtered.length === 0
              ? <tr><td colSpan={10}><EmptyState message="No stations match your filters" icon={MapPin} /></td></tr>
              : filtered.map((s) => (
              <tr key={s.id} className={`hover:bg-[#F4F8FC] transition-colors ${!stationReady(s) ? "bg-[#FDEDEB]/20" : ""}`}>
                <Td><MonoCode>{s.code}</MonoCode></Td>
                <Td>
                  <p className="font-semibold text-[#1D2433]">{s.name}</p>
                  <p className="text-[11px] text-[#6B7280] truncate max-w-[160px]">{s.address}</p>
                </Td>
                <Td>
                  <span className="px-2 py-0.5 text-[11px] font-bold rounded text-white" style={{ backgroundColor: s.lineColor }}>{s.line}</span>
                </Td>
                <Td><Badge status={s.status} dot /></Td>
                <Td><ReadinessBadge gpsReady={s.gpsReady} scanKeyConfigured={s.scanKeyStatus === "CONFIGURED"} /></Td>
                <Td>
                  {s.gpsReady
                    ? <Badge status="GPS_OK" dot />
                    : <Badge status="GPS_MISSING" dot />}
                </Td>
                <Td>
                  {s.scanKeyStatus === "CONFIGURED"
                    ? <Badge status="SCAN_KEY_OK" dot />
                    : <Badge status="SCAN_KEY_MISSING" dot />}
                </Td>
                <Td className="font-mono text-xs font-medium">{s.collectors.toLocaleString()}</Td>
                <Td className="text-[11px] text-[#6B7280] font-mono">{s.lastScanAt}</Td>
                <Td>
                  <div className="flex items-center gap-1">
                    <button onClick={onViewDetail} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Eye size={13} /></button>
                    <button onClick={() => setDrawerOpen(true)} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Pencil size={13} /></button>
                    <button className="p-1 rounded hover:bg-[#FDEDEB] text-[#6B7280] hover:text-[#E83B28]"><Trash2 size={13} /></button>
                  </div>
                </Td>
              </tr>
            ))}
          </tbody>
        </TableWrapper>
        <Pagination shown={filtered.length} total={STATIONS.length} />
      </Card>
      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Add Station" subtitle="Station will be created in DRAFT status with no scan keys">
        <DrawerSection title="Line Assignment" />
        <SelectField label="Metro Line" value="ML1" onChange={() => {}} required options={METRO_LINES.map(l => ({ label: l.name, value: l.code }))} />
        <DrawerSection title="Identity" />
        <Input label="Station Code" placeholder="VT" value="" onChange={() => {}} required mono hint="Unique uppercase code for this station" />
        <Input label="Name" placeholder="Văn Thánh" value="" onChange={() => {}} required />
        <Input label="Display Name (Vietnamese)" placeholder="Văn Thánh" value="" onChange={() => {}} />
        <Input label="Address" placeholder="Bình Thạnh, TP.HCM" value="" onChange={() => {}} />
        <Input label="Sort Order" placeholder="4" value="" onChange={() => {}} type="number" />
        <DrawerSection title="GPS / Geofence" />
        <div className="grid grid-cols-2 gap-3">
          <Input label="Latitude" placeholder="10.8030" value="" onChange={() => {}} mono />
          <Input label="Longitude" placeholder="106.7120" value="" onChange={() => {}} mono />
        </div>
        <Input label="Zone Radius (metres)" placeholder="100" value="" onChange={() => {}} type="number" hint="Recommended: 80–150m depending on station layout" />
        <DrawerSection title="Assets" />
        <div className="flex flex-col gap-1">
          <label className="text-xs font-semibold text-[#6B7280] uppercase tracking-wide">Station Image URL</label>
          <div className="flex gap-2">
            <input placeholder="https://cdn.exoticstamp.vn/…" className="flex-1 px-3 py-2 text-sm bg-white border border-[#E4E7EC] rounded focus:outline-none focus:ring-2 focus:ring-[#01599D] font-mono text-xs" />
            <Btn variant="outline" size="sm" onClick={() => setAssetUploadOpen(true)}><Upload size={13} /> Upload</Btn>
          </div>
        </div>
        <DrawerSection title="Status" />
        <SelectField label="Status" value="DRAFT" onChange={() => {}} options={[{label:"Draft",value:"DRAFT"},{label:"Active",value:"ACTIVE"}]} />
      </Drawer>
      <AssetUploadModal open={assetUploadOpen} onClose={() => setAssetUploadOpen(false)} />
    </div>
  );
}

// ─── Station Detail ───────────────────────────────────────────────────────────

function StationDetailScreen() {
  const station = STATIONS[0];
  const [rotateConfirm, setRotateConfirm] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [editOpen, setEditOpen] = useState(false);

  return (
    <div>
      <Breadcrumb items={["Stations", station.name]} />
      <div className="flex items-start justify-between mb-5">
        <div>
          <div className="flex items-center gap-2.5 mb-1">
            <h2 className="text-lg font-bold text-[#1D2433]">{station.name}</h2>
            <span className="px-2 py-0.5 text-[11px] font-bold rounded text-white" style={{ backgroundColor: station.lineColor }}>{station.line}</span>
            <Badge status={station.status} dot />
            <ReadinessBadge gpsReady={station.gpsReady} scanKeyConfigured={station.scanKeyStatus === "CONFIGURED"} />
          </div>
          <p className="text-xs text-[#6B7280] font-mono">{station.code} · {station.address}</p>
        </div>
        <div className="flex items-center gap-2">
          <Btn variant="secondary" size="sm" onClick={() => setDeleteConfirm(true)}><Trash2 size={13} /> Soft Delete</Btn>
          <Btn variant="primary" size="sm" onClick={() => setEditOpen(true)}><Pencil size={13} /> Edit Station</Btn>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-4">
        <Card className="p-4">
          <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-3">Station Information</p>
          <KV label="Station Code" value={<MonoCode>{station.code}</MonoCode>} />
          <KV label="Name" value={station.name} />
          <KV label="Display Name" value={station.name} />
          <KV label="Address" value={station.address} />
          <KV label="Sort Order" value="1" mono />
          <KV label="Status" value={<Badge status={station.status} dot />} />
          <AuditRow updatedAt={station.updatedAt} updatedBy={station.updatedBy} />
        </Card>

        <Card className="p-4">
          <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-3">GPS / Geofence Configuration</p>
          <KV label="GPS Status" value={station.gpsReady ? <Badge status="GPS_OK" dot /> : <Badge status="GPS_MISSING" dot />} />
          <KV label="Latitude" value={<MonoCode>{station.lat}</MonoCode>} />
          <KV label="Longitude" value={<MonoCode>{station.lng}</MonoCode>} />
          <KV label="Zone Radius" value={<MonoCode>{station.radius}m</MonoCode>} />
          <div className="mt-3 bg-[#F4F8FC] rounded border border-[#E4E7EC] h-24 flex items-center justify-center">
            <div className="text-center">
              <MapPin size={18} className="mx-auto mb-1 text-[#01599D]" />
              <p className="text-[11px] text-[#6B7280]">Map preview · {station.lat}, {station.lng}</p>
              <p className="text-[10px] text-[#6B7280]">Geofence radius {station.radius}m</p>
            </div>
          </div>
        </Card>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Card className="p-4">
          <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-3">Scan Key Management</p>
          <KV label="Key Status" value={station.scanKeyStatus === "CONFIGURED" ? <Badge status="SCAN_KEY_OK" dot /> : <Badge status="SCAN_KEY_MISSING" dot />} />
          <div className="space-y-3 mt-3">
            <MaskedValue label="NFC Tag ID" value="NFC:04:AB:CD:12:34:56:78" />
            <MaskedValue label="QR Code Value" value="ESMS:BT:2024:k8j2h9s1q3" />
          </div>
          <div className="grid grid-cols-2 gap-3 mt-4 pt-3 border-t border-[#E4E7EC]">
            <div>
              <p className="text-[10px] text-[#6B7280] font-semibold uppercase tracking-wide">Last QR Rotated</p>
              <p className="text-xs text-[#1D2433] font-mono mt-0.5">2024-06-01 09:00</p>
            </div>
            <div>
              <p className="text-[10px] text-[#6B7280] font-semibold uppercase tracking-wide">Last Scan Detected</p>
              <p className="text-xs text-[#1D2433] font-mono mt-0.5">{station.lastScanAt}</p>
            </div>
          </div>
          <div className="flex gap-2 mt-4 pt-3 border-t border-[#E4E7EC]">
            <Btn variant="secondary" size="sm"><RefreshCw size={12} /> Update Keys</Btn>
            <Btn variant="danger" size="sm" onClick={() => setRotateConfirm(true)}><QrCode size={12} /> Rotate QR Code</Btn>
          </div>
        </Card>

        <Card className="p-4">
          <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-3">Public Assets</p>
          <div className="space-y-3">
            <div>
              <p className="text-[10px] text-[#6B7280] font-semibold uppercase tracking-wide mb-2">Station Image</p>
              <div className="bg-[#F4F8FC] rounded border border-[#E4E7EC] aspect-video overflow-hidden">
                <img src="https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=480&h=270&fit=crop&auto=format" alt="Bến Thành station" className="w-full h-full object-cover" />
              </div>
            </div>
            <div>
              <p className="text-[10px] text-[#6B7280] font-semibold uppercase tracking-wide mb-2">Stamp Preview Image</p>
              <div className="bg-[#F4F8FC] rounded border border-[#E4E7EC] h-16 flex items-center justify-center">
                <div className="text-center"><Stamp size={20} className="mx-auto text-[#01599D] opacity-30 mb-0.5" /><p className="text-[10px] text-[#6B7280]">No stamp preview set</p></div>
              </div>
            </div>
          </div>
        </Card>
      </div>

      <ConfirmModal open={rotateConfirm} onClose={() => setRotateConfirm(false)} onConfirm={() => {}}
        title={`Rotate QR Code — ${station.name} (${station.code})`}
        message={`This will immediately invalidate the current QR code for station ${station.code}. Active users mid-scan will fail. The new QR code must be reprinted and installed at the physical station before it can accept new scans. This cannot be undone.`}
        confirmLabel="Rotate QR Code" dangerous />
      <ConfirmModal open={deleteConfirm} onClose={() => setDeleteConfirm(false)} onConfirm={() => {}}
        title={`Soft-delete Station — ${station.name}`}
        message={`Station ${station.code} will be hidden from the mobile app and excluded from all active campaigns. Existing stamp records for this station are retained. An admin can restore this station later.`}
        confirmLabel="Delete Station" dangerous />
      <Drawer open={editOpen} onClose={() => setEditOpen(false)} title={`Edit Station — ${station.name}`} subtitle={`${station.code} · ${station.line}`}>
        <DrawerSection title="Identity" />
        <Input label="Name" value={station.name} onChange={() => {}} required />
        <Input label="Display Name" value={station.name} onChange={() => {}} />
        <Input label="Address" value={station.address} onChange={() => {}} />
        <DrawerSection title="GPS / Geofence" />
        <div className="grid grid-cols-2 gap-3">
          <Input label="Latitude" value={station.lat} onChange={() => {}} mono />
          <Input label="Longitude" value={station.lng} onChange={() => {}} mono />
        </div>
        <Input label="Zone Radius (metres)" value={station.radius.toString()} onChange={() => {}} type="number" />
        <DrawerSection title="Status" />
        <SelectField label="Status" value={station.status} onChange={() => {}} options={[{label:"Active",value:"ACTIVE"},{label:"Draft",value:"DRAFT"},{label:"Inactive",value:"INACTIVE"}]} />
      </Drawer>
    </div>
  );
}

// ─── Campaigns ────────────────────────────────────────────────────────────────

function CampaignsScreen({ onViewDetail }: { onViewDetail: () => void }) {
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [drawerOpen, setDrawerOpen] = useState(false);

  const filtered = CAMPAIGNS.filter((c) =>
    (typeFilter === "ALL" || c.type === typeFilter) &&
    (statusFilter === "ALL" || c.status === statusFilter) &&
    (c.name.toLowerCase().includes(search.toLowerCase()) || c.code.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <p className="text-xs text-[#6B7280]"><span className="font-semibold text-[#1D2433]">{CAMPAIGNS.filter(c => c.status === "ACTIVE").length}</span> active · <span className="font-semibold text-[#6B7280]">{CAMPAIGNS.filter(c => c.status === "DRAFT").length}</span> draft</p>
        <Btn variant="primary" size="sm" onClick={() => setDrawerOpen(true)}><Plus size={14} /> Create Campaign</Btn>
      </div>
      <Card>
        <FilterBar>
          <SearchInput value={search} onChange={setSearch} placeholder="Search campaign code or name…" />
          <FilterSelect value={typeFilter} onChange={setTypeFilter} options={[["ALL","All Types"],["STANDARD","Standard"],["SEASONAL","Seasonal"],["EVENT","Event"]]} />
          <FilterSelect value={statusFilter} onChange={setStatusFilter} options={[["ALL","All Status"],["ACTIVE","Active"],["DRAFT","Draft"],["ARCHIVED","Archived"]]} />
        </FilterBar>
        <TableWrapper>
          <thead><tr>
            <Th>Code</Th><Th>Campaign Name</Th><Th>Type</Th><Th>Status</Th>
            <Th>Start Date</Th><Th>End Date</Th><Th>Days Left</Th><Th>Priority</Th><Th>Stations</Th><Th>Last Updated</Th><Th>Actions</Th>
          </tr></thead>
          <tbody>
            {filtered.map((c) => {
              const days = daysUntil(c.endDate);
              const noStationWarn = c.status === "ACTIVE" && c.stations === 0;
              return (
                <tr key={c.id} className={`hover:bg-[#F4F8FC] transition-colors ${noStationWarn ? "bg-amber-50/40" : ""}`}>
                  <Td><MonoCode>{c.code}</MonoCode></Td>
                  <Td>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold">{c.name}</span>
                      {noStationWarn && <AlertTriangle size={12} className="text-amber-500 shrink-0" />}
                    </div>
                    <p className="text-[11px] text-[#6B7280]">{c.displayName}</p>
                  </Td>
                  <Td><Badge status={c.type} /></Td>
                  <Td><Badge status={c.status} dot /></Td>
                  <Td className="text-xs text-[#6B7280] font-mono">{c.startDate}</Td>
                  <Td className="text-xs text-[#6B7280] font-mono">{c.endDate}</Td>
                  <Td>
                    {c.status === "ACTIVE" && days > 0 ? (
                      <span className={`text-xs font-mono font-semibold ${days < 14 ? "text-[#E83B28]" : "text-[#1D2433]"}`}>{days}d</span>
                    ) : c.status === "ARCHIVED" ? <span className="text-[11px] text-[#6B7280]">ended</span>
                    : <span className="text-[11px] text-[#6B7280]">—</span>}
                  </Td>
                  <Td className="font-mono text-xs">{c.priority}</Td>
                  <Td>
                    {c.stations === 0 && c.status !== "ARCHIVED"
                      ? <span className="text-xs text-amber-600 font-semibold">0 — unassigned</span>
                      : <span className="text-xs font-mono">{c.stations}</span>}
                  </Td>
                  <Td>
                    <p className="text-[11px] text-[#6B7280]">{c.updatedAt}</p>
                    <p className="text-[10px] text-[#6B7280] font-mono">{c.updatedBy}</p>
                  </Td>
                  <Td>
                    <div className="flex items-center gap-1">
                      <button onClick={onViewDetail} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Eye size={13} /></button>
                      <button onClick={() => setDrawerOpen(true)} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Pencil size={13} /></button>
                      <button className="p-1 rounded hover:bg-[#FDEDEB] text-[#6B7280] hover:text-[#E83B28]"><Trash2 size={13} /></button>
                    </div>
                  </Td>
                </tr>
              );
            })}
          </tbody>
        </TableWrapper>
        <Pagination shown={filtered.length} total={CAMPAIGNS.length} />
      </Card>
      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Create Campaign" subtitle="New campaign will be in DRAFT until manually activated">
        <DrawerSection title="Identity" />
        <Input label="Code" placeholder="SUM25" value="" onChange={() => {}} required mono hint="Short uppercase code, unique per campaign" />
        <Input label="Name" placeholder="Summer Metro 2025" value="" onChange={() => {}} required />
        <Input label="Display Name (Vietnamese)" placeholder="Hè Metro 2025" value="" onChange={() => {}} />
        <SelectField label="Type" value="STANDARD" onChange={() => {}} required options={[{label:"Standard",value:"STANDARD"},{label:"Seasonal",value:"SEASONAL"},{label:"Event",value:"EVENT"}]} />
        <DrawerSection title="Schedule" />
        <div className="grid grid-cols-2 gap-3">
          <Input label="Start Date" value="" onChange={() => {}} type="date" required />
          <Input label="End Date" value="" onChange={() => {}} type="date" required hint="Must be after start date" />
        </div>
        <Input label="Priority" placeholder="1" value="" onChange={() => {}} type="number" hint="Lower number = higher priority" />
        <DrawerSection title="Status" />
        <SelectField label="Status" value="DRAFT" onChange={() => {}} options={[{label:"Draft",value:"DRAFT"},{label:"Active",value:"ACTIVE"}]} />
      </Drawer>
    </div>
  );
}

// ─── Campaign Detail ──────────────────────────────────────────────────────────

function CampaignDetailScreen() {
  const campaign = CAMPAIGNS[0];
  const [archiveConfirm, setArchiveConfirm] = useState(false);
  const daysLeft = daysUntil(campaign.endDate);

  return (
    <div>
      <Breadcrumb items={["Campaigns", campaign.name]} />
      <div className="flex items-start justify-between mb-5">
        <div>
          <div className="flex items-center gap-2.5 mb-1">
            <h2 className="text-lg font-bold text-[#1D2433]">{campaign.name}</h2>
            <Badge status={campaign.type} />
            <Badge status={campaign.status} dot />
            {daysLeft > 0 && daysLeft < 14 && <Badge status="NOT_READY" label={`${daysLeft}d remaining`} />}
          </div>
          <p className="text-xs text-[#6B7280] font-mono">{campaign.code} · {campaign.startDate} → {campaign.endDate}</p>
          <AuditRow updatedAt={campaign.updatedAt} updatedBy={campaign.updatedBy} />
        </div>
        <div className="flex items-center gap-2">
          <Btn variant="secondary" size="sm" onClick={() => setArchiveConfirm(true)}>Archive</Btn>
          <Btn variant="primary" size="sm"><Pencil size={13} /> Edit Campaign</Btn>
        </div>
      </div>

      {campaign.stations === 0 && (
        <AlertBanner type="warn">
          <strong>No stations assigned.</strong> This campaign cannot accept stamp scans until at least one station is assigned. Assign stations before activating.
        </AlertBanner>
      )}

      <div className="grid grid-cols-3 gap-4 mb-4">
        <Card className="col-span-2">
          <div className="flex items-center justify-between px-4 py-3 border-b border-[#E4E7EC]">
            <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest">Assigned Stations ({campaign.stations})</p>
            <Btn variant="secondary" size="sm"><Plus size={13} /> Add Station</Btn>
          </div>
          <TableWrapper>
            <thead><tr><Th>Code</Th><Th>Station Name</Th><Th>Line</Th><Th>Readiness</Th><Th>Sort</Th><Th>Actions</Th></tr></thead>
            <tbody>
              {STATIONS.slice(0, 5).map((s) => (
                <tr key={s.id} className="hover:bg-[#F4F8FC]">
                  <Td><MonoCode>{s.code}</MonoCode></Td>
                  <Td className="font-semibold">{s.name}</Td>
                  <Td><span className="px-1.5 py-0.5 text-[11px] font-bold rounded text-white" style={{ backgroundColor: s.lineColor }}>{s.line}</span></Td>
                  <Td><ReadinessBadge gpsReady={s.gpsReady} scanKeyConfigured={s.scanKeyStatus === "CONFIGURED"} /></Td>
                  <Td className="font-mono text-xs">{s.id}</Td>
                  <Td><button className="text-[11px] text-[#E83B28] font-semibold hover:underline">Remove</button></Td>
                </tr>
              ))}
            </tbody>
          </TableWrapper>
        </Card>

        <div className="space-y-4">
          <Card className="p-4">
            <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-3">Campaign Info</p>
            <KV label="Code" value={<MonoCode>{campaign.code}</MonoCode>} />
            <KV label="Display Name" value={campaign.displayName} />
            <KV label="Priority" value={campaign.priority.toString()} mono />
            <KV label="Days Remaining" value={<span className={daysLeft < 14 ? "text-[#E83B28] font-bold" : ""}>{daysLeft}d</span>} />
          </Card>
          <Card className="p-4">
            <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-2">Banner Image</p>
            <div className="bg-[#F4F8FC] rounded border border-[#E4E7EC] aspect-video overflow-hidden">
              <img src="https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=480&h=270&fit=crop&auto=format" alt="Campaign banner" className="w-full h-full object-cover" />
            </div>
          </Card>
        </div>
      </div>

      <ConfirmModal open={archiveConfirm} onClose={() => setArchiveConfirm(false)} onConfirm={() => {}}
        title={`Archive Campaign — ${campaign.name}`}
        message={`Archiving this campaign will immediately remove it from the mobile app. All stamps collected under this campaign are permanently retained. New scans will be rejected. This action cannot be reversed without admin support.`}
        confirmLabel="Archive Campaign" dangerous />
    </div>
  );
}

// ─── Stamp Designs ────────────────────────────────────────────────────────────

function StampDesignsScreen() {
  const [viewMode, setViewMode] = useState<"grid" | "table">("grid");
  const [rarityFilter, setRarityFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const rarityColor: Record<string, string> = { COMMON: "#6B7280", RARE: "#01599D", EPIC: "#8B5CF6", LEGENDARY: "#F59E0B" };

  const filtered = STAMP_DESIGNS.filter((s) =>
    (rarityFilter === "ALL" || s.rarity === rarityFilter) &&
    (statusFilter === "ALL" || s.status === statusFilter)
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          {["grid","table"].map((m) => (
            <button key={m} onClick={() => setViewMode(m as "grid"|"table")} className={`px-3 py-1.5 text-xs font-semibold rounded border transition-colors ${viewMode === m ? "bg-[#01599D] text-white border-[#01599D]" : "border-[#E4E7EC] text-[#6B7280] hover:bg-[#F4F8FC]"}`}>
              {m.charAt(0).toUpperCase() + m.slice(1)}
            </button>
          ))}
          <FilterSelect value={rarityFilter} onChange={setRarityFilter} options={[["ALL","All Rarities"],["COMMON","Common"],["RARE","Rare"],["EPIC","Epic"],["LEGENDARY","Legendary"]]} />
          <FilterSelect value={statusFilter} onChange={setStatusFilter} options={[["ALL","All Status"],["ACTIVE","Active"],["DRAFT","Draft"]]} />
        </div>
        <Btn variant="primary" size="sm" onClick={() => setDrawerOpen(true)}><Plus size={14} /> Create Stamp</Btn>
      </div>

      {viewMode === "grid" ? (
        <div className="grid grid-cols-3 gap-4">
          {filtered.map((s) => (
            <Card key={s.id} className="overflow-hidden hover:border-[#01599D] transition-colors group cursor-pointer">
              <div className="bg-[#F4F8FC] h-32 flex items-center justify-center border-b border-[#E4E7EC] relative">
                <div className="w-16 h-16 rounded-full border-4 flex items-center justify-center" style={{ borderColor: rarityColor[s.rarity] + "50", backgroundColor: rarityColor[s.rarity] + "12" }}>
                  <Stamp size={28} style={{ color: rarityColor[s.rarity] }} />
                </div>
                <div className="absolute top-2 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button className="p-1 bg-white rounded border border-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Pencil size={11} /></button>
                  <button className="p-1 bg-white rounded border border-[#E4E7EC] text-[#6B7280] hover:text-[#E83B28]"><Trash2 size={11} /></button>
                </div>
              </div>
              <div className="p-3">
                <p className="text-sm font-semibold text-[#1D2433] mb-0.5">{s.name}</p>
                <p className="text-[11px] text-[#6B7280] mb-2">{s.campaign}</p>
                <div className="flex items-center justify-between">
                  <Badge status={s.rarity} />
                  <Badge status={s.status} dot />
                </div>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card>
          <TableWrapper>
            <thead><tr><Th>Preview</Th><Th>Name</Th><Th>Campaign</Th><Th>Station</Th><Th>Rarity</Th><Th>Status</Th><Th>Sort</Th><Th>Updated</Th><Th>Actions</Th></tr></thead>
            <tbody>
              {filtered.map((s) => (
                <tr key={s.id} className="hover:bg-[#F4F8FC]">
                  <Td>
                    <div className="w-8 h-8 rounded-full border-2 flex items-center justify-center" style={{ borderColor: rarityColor[s.rarity] + "60", backgroundColor: rarityColor[s.rarity] + "12" }}>
                      <Stamp size={14} style={{ color: rarityColor[s.rarity] }} />
                    </div>
                  </Td>
                  <Td className="font-semibold">{s.name}</Td>
                  <Td className="text-[#6B7280] text-xs">{s.campaign}</Td>
                  <Td className="text-[#6B7280] text-xs">{s.station}</Td>
                  <Td><Badge status={s.rarity} /></Td>
                  <Td><Badge status={s.status} dot /></Td>
                  <Td className="font-mono text-xs">{s.sortOrder}</Td>
                  <Td className="text-[11px] text-[#6B7280] font-mono">{s.updatedAt}</Td>
                  <Td><div className="flex gap-1">
                    <button className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Pencil size={13} /></button>
                    <button className="p-1 rounded hover:bg-[#FDEDEB] text-[#6B7280] hover:text-[#E83B28]"><Trash2 size={13} /></button>
                  </div></Td>
                </tr>
              ))}
            </tbody>
          </TableWrapper>
          <Pagination shown={filtered.length} total={STAMP_DESIGNS.length} />
        </Card>
      )}

      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Create Stamp Design" subtitle="Assign to a campaign and optionally a specific station">
        <DrawerSection title="Assignment" />
        <SelectField label="Campaign" value="" onChange={() => {}} required options={CAMPAIGNS.map(c => ({ label: c.name, value: c.id }))} />
        <SelectField label="Station (optional)" value="" onChange={() => {}} options={[{label:"(All stations in campaign)",value:""},...STATIONS.map(s=>({label:s.name,value:s.id}))]} />
        <DrawerSection title="Design" />
        <Input label="Name" placeholder="Bến Thành Classic" value="" onChange={() => {}} required />
        <Input label="Description" placeholder="A classic stamp…" value="" onChange={() => {}} />
        <SelectField label="Rarity" value="COMMON" onChange={() => {}} required options={[{label:"Common",value:"COMMON"},{label:"Rare",value:"RARE"},{label:"Epic",value:"EPIC"},{label:"Legendary",value:"LEGENDARY"}]} />
        <DrawerSection title="Assets" />
        <Input label="Image URL" placeholder="https://cdn.exoticstamp.vn/…" value="" onChange={() => {}} mono />
        <Input label="Preview Image URL" placeholder="https://cdn.exoticstamp.vn/…" value="" onChange={() => {}} mono />
        <DrawerSection title="Publishing" />
        <Input label="Sort Order" value="1" onChange={() => {}} type="number" />
        <SelectField label="Status" value="DRAFT" onChange={() => {}} required options={[{label:"Draft",value:"DRAFT"},{label:"Active",value:"ACTIVE"}]} />
      </Drawer>
    </div>
  );
}

// ─── Partners ─────────────────────────────────────────────────────────────────

function PartnersScreen() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [deactivateConfirm, setDeactivateConfirm] = useState<string | null>(null);

  const contractStatus = (p: typeof PARTNERS[0]) => {
    if (!p.active) return "INACTIVE";
    const days = daysUntil(p.contractEnd);
    if (days < 0) return "EXPIRED_CONTRACT";
    if (days < 60) return "EXPIRING";
    return "CONTRACT_ACTIVE";
  };

  return (
    <div>
      <div className="flex justify-end mb-4">
        <Btn variant="primary" size="sm" onClick={() => setDrawerOpen(true)}><Plus size={14} /> Add Partner</Btn>
      </div>
      <Card>
        <TableWrapper>
          <thead><tr><Th>Partner</Th><Th>Contact Email</Th><Th>Contract Start</Th><Th>Contract End</Th><Th>Days Left</Th><Th>Contract Status</Th><Th>Active</Th><Th>Actions</Th></tr></thead>
          <tbody>
            {PARTNERS.map((p) => {
              const cs = contractStatus(p);
              const days = daysUntil(p.contractEnd);
              return (
                <tr key={p.id} className="hover:bg-[#F4F8FC]">
                  <Td>
                    <div className="flex items-center gap-2.5">
                      <div className="w-8 h-8 rounded bg-[#F4F8FC] border border-[#E4E7EC] flex items-center justify-center shrink-0">
                        <Building2 size={14} className="text-[#6B7280]" />
                      </div>
                      <span className="font-semibold text-[#1D2433]">{p.name}</span>
                    </div>
                  </Td>
                  <Td className="text-xs text-[#6B7280] font-mono">{p.email}</Td>
                  <Td className="text-xs text-[#6B7280] font-mono">{p.contractStart}</Td>
                  <Td className="text-xs text-[#6B7280] font-mono">{p.contractEnd}</Td>
                  <Td>
                    {cs === "EXPIRING" ? <span className="text-xs font-bold text-amber-700">{days}d</span>
                    : cs === "EXPIRED_CONTRACT" ? <span className="text-xs font-bold text-[#E83B28]">expired</span>
                    : <span className="text-xs text-[#6B7280] font-mono">{days > 0 ? `${days}d` : "—"}</span>}
                  </Td>
                  <Td><Badge status={cs} dot /></Td>
                  <Td>{p.active ? <CheckCircle2 size={15} className="text-emerald-600" /> : <XCircle size={15} className="text-[#6B7280]" />}</Td>
                  <Td>
                    <div className="flex gap-1 items-center">
                      <button onClick={() => setDrawerOpen(true)} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Pencil size={13} /></button>
                      <button onClick={() => setDeactivateConfirm(p.name)}
                        className={`text-[11px] font-semibold px-2 py-0.5 rounded hover:opacity-80 ${p.active ? "text-[#E83B28]" : "text-emerald-700"}`}>
                        {p.active ? "Deactivate" : "Activate"}
                      </button>
                    </div>
                  </Td>
                </tr>
              );
            })}
          </tbody>
        </TableWrapper>
      </Card>
      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Add Partner" subtitle="Partner data is used in reward configuration">
        <DrawerSection title="Identity" />
        <Input label="Partner Name" placeholder="Grab Vietnam" value="" onChange={() => {}} required />
        <Input label="Contact Email" placeholder="partner@example.com" value="" onChange={() => {}} type="email" required />
        <Input label="Logo URL" placeholder="https://cdn.exoticstamp.vn/…" value="" onChange={() => {}} mono />
        <DrawerSection title="Contract" />
        <div className="grid grid-cols-2 gap-3">
          <Input label="Contract Start" value="" onChange={() => {}} type="date" required />
          <Input label="Contract End" value="" onChange={() => {}} type="date" required />
        </div>
      </Drawer>
      <ConfirmModal open={!!deactivateConfirm} onClose={() => setDeactivateConfirm(null)} onConfirm={() => {}}
        title={`Deactivate Partner — ${deactivateConfirm}`}
        message={`Deactivating ${deactivateConfirm} will flag all associated rewards as inactive. Active vouchers already issued remain valid until their expiry date. This affects all future reward issuance from this partner.`}
        confirmLabel="Deactivate Partner" dangerous />
    </div>
  );
}

// ─── Milestones ───────────────────────────────────────────────────────────────

function MilestonesScreen() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const stdMilestones = MILESTONES.filter(m => m.campaign === "Standard Collection");

  return (
    <div>
      <div className="flex justify-end mb-4">
        <Btn variant="primary" size="sm" onClick={() => setDrawerOpen(true)}><Plus size={14} /> Create Milestone</Btn>
      </div>

      {/* Timeline preview */}
      <Card className="p-5 mb-4">
        <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-5">Milestone Timeline — Standard Collection</p>
        <div className="relative">
          <div className="absolute top-5 left-8 right-8 h-px bg-[#E4E7EC]" />
          <div className="flex">
            {stdMilestones.map((m, i) => (
              <div key={i} className="flex-1 flex flex-col items-center gap-2 relative z-10">
                <div className="w-10 h-10 rounded-full bg-[#01599D] text-white flex items-center justify-center text-sm font-bold border-2 border-white shadow">
                  {m.requiredStamps}
                </div>
                <p className="text-xs font-semibold text-[#1D2433] text-center">{m.name}</p>
                <Badge status={m.rewardType} />
                <p className="text-[10px] text-[#6B7280] text-center">{m.rewardTitle}</p>
              </div>
            ))}
          </div>
        </div>
      </Card>

      <Card>
        <TableWrapper>
          <thead><tr>
            <Th>Code</Th><Th>Name</Th><Th>Campaign</Th><Th>Required Stamps</Th>
            <Th>Reward Type</Th><Th>Reward Title</Th><Th>Status</Th><Th>Sort</Th><Th>Actions</Th>
          </tr></thead>
          <tbody>
            {MILESTONES.map((m) => (
              <tr key={m.id} className="hover:bg-[#F4F8FC]">
                <Td><MonoCode>{m.code}</MonoCode></Td>
                <Td className="font-semibold">{m.name}</Td>
                <Td className="text-xs text-[#6B7280]">{m.campaign}</Td>
                <Td>
                  <span className="inline-flex items-center gap-1">
                    <span className="text-base font-bold text-[#01599D] font-mono">{m.requiredStamps}</span>
                    <span className="text-xs text-[#6B7280]">stamps</span>
                  </span>
                </Td>
                <Td><Badge status={m.rewardType} /></Td>
                <Td className="text-xs">{m.rewardTitle}</Td>
                <Td><Badge status={m.status} dot /></Td>
                <Td className="font-mono text-xs">{m.sortOrder}</Td>
                <Td><div className="flex gap-1">
                  <button onClick={() => setDrawerOpen(true)} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Pencil size={13} /></button>
                  <button className="p-1 rounded hover:bg-[#FDEDEB] text-[#6B7280] hover:text-[#E83B28]"><Trash2 size={13} /></button>
                </div></Td>
              </tr>
            ))}
          </tbody>
        </TableWrapper>
      </Card>

      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Create Milestone" subtitle="Milestone triggers reward issuance when a user reaches the stamp count">
        <DrawerSection title="Campaign" />
        <SelectField label="Campaign" value="" onChange={() => {}} required options={CAMPAIGNS.map(c=>({label:c.name,value:c.id}))} />
        <DrawerSection title="Trigger" />
        <Input label="Code" placeholder="MS10" value="" onChange={() => {}} required mono hint="Unique milestone code, e.g. MS3, MS7, MS14" />
        <Input label="Required Stamp Count" placeholder="10" value="" onChange={() => {}} type="number" required hint="User must collect this many stamps to unlock the reward" />
        <Input label="Name" placeholder="First Trio" value="" onChange={() => {}} required />
        <Input label="Description" placeholder="Collect stamps from 3 different stations" value="" onChange={() => {}} />
        <DrawerSection title="Reward" />
        <SelectField label="Reward Type" value="VOUCHER" onChange={() => {}} required options={[{label:"Voucher",value:"VOUCHER"},{label:"Digital Sticker",value:"DIGITAL_STICKER"},{label:"Bonus Stamp",value:"BONUS_STAMP"}]} />
        <Input label="Reward Title" placeholder="Coffee Voucher 20k" value="" onChange={() => {}} required />
        <Input label="Reward Description" placeholder="Redeem at any Highlands Coffee" value="" onChange={() => {}} />
        <DrawerSection title="Publishing" />
        <Input label="Sort Order" value="1" onChange={() => {}} type="number" />
        <SelectField label="Status" value="DRAFT" onChange={() => {}} options={[{label:"Draft",value:"DRAFT"},{label:"Active",value:"ACTIVE"}]} />
      </Drawer>
    </div>
  );
}

// ─── Rewards & Vouchers ───────────────────────────────────────────────────────

function RewardsScreen() {
  const [tab, setTab] = useState<"rewards"|"vouchers"|"import">("rewards");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [importDone, setImportDone] = useState(false);
  const [revealedVouchers, setRevealedVouchers] = useState<Set<string>>(new Set());
  const toggleReveal = (id: string) => setRevealedVouchers((prev) => { const n = new Set(prev); n.has(id) ? n.delete(id) : n.add(id); return n; });

  return (
    <div>
      {lowStockRewards.length > 0 && (
        <AlertBanner type="warn">
          <strong>{lowStockRewards.length} reward{lowStockRewards.length>1?"s":""} low on voucher stock:</strong> {lowStockRewards.map(r=>`${r.name} (${r.totalStock - r.issued} remaining)`).join(", ")}. Import new voucher codes before stock depletes.
        </AlertBanner>
      )}
      <div className="flex items-center gap-1 mb-4 border-b border-[#E4E7EC]">
        {(["rewards","vouchers","import"] as const).map((t) => (
          <button key={t} onClick={() => setTab(t)} className={`px-4 py-2.5 text-xs font-bold uppercase tracking-wide border-b-2 transition-colors ${tab===t?"border-[#01599D] text-[#01599D]":"border-transparent text-[#6B7280] hover:text-[#1D2433]"}`}>
            {t === "rewards" ? "Rewards" : t === "vouchers" ? "Voucher Pool" : "Import Vouchers"}
          </button>
        ))}
        <div className="ml-auto pb-2">
          {tab === "rewards" && <Btn variant="primary" size="sm" onClick={() => setDrawerOpen(true)}><Plus size={14} /> Create Reward</Btn>}
        </div>
      </div>

      {tab === "rewards" && (
        <Card>
          <TableWrapper>
            <thead><tr>
              <Th>Name</Th><Th>Type</Th><Th>Partner</Th><Th>Milestone</Th>
              <Th>Value</Th><Th>Expiry</Th><Th>Stock</Th><Th>Stock Status</Th><Th>Active</Th><Th>Actions</Th>
            </tr></thead>
            <tbody>
              {REWARDS.map((r) => {
                const remaining = r.totalStock - r.issued;
                return (
                  <tr key={r.id} className={`hover:bg-[#F4F8FC] ${stockPct(r) <= 20 && r.type === "VOUCHER" && r.active ? "bg-amber-50/30" : ""}`}>
                    <Td className="font-semibold">{r.name}</Td>
                    <Td><Badge status={r.type} /></Td>
                    <Td className="text-xs text-[#6B7280]">{r.partner}</Td>
                    <Td className="text-xs text-[#6B7280]">{r.milestone}</Td>
                    <Td className="font-mono text-xs">{r.value > 0 ? `₫${r.value.toLocaleString()}` : "—"}</Td>
                    <Td className="text-xs text-[#6B7280]">{r.expiryDays > 0 ? `${r.expiryDays} days` : "—"}</Td>
                    <Td>
                      <div className="flex items-center gap-2">
                        <div className="w-14 h-1.5 bg-[#E4E7EC] rounded-full overflow-hidden">
                          <div className="h-full rounded-full" style={{ width: `${stockPct(r)}%`, backgroundColor: stockPct(r) > 50 ? "#10B981" : stockPct(r) > 20 ? "#F59E0B" : "#E83B28" }} />
                        </div>
                        <span className="text-[11px] font-mono text-[#6B7280]">{remaining}/{r.totalStock}</span>
                      </div>
                    </Td>
                    <Td><StockBadge total={r.totalStock} issued={r.issued} /></Td>
                    <Td>{r.active ? <CheckCircle2 size={14} className="text-emerald-600" /> : <XCircle size={14} className="text-[#6B7280]" />}</Td>
                    <Td><div className="flex gap-1 items-center">
                      <button onClick={() => setDrawerOpen(true)} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Pencil size={13} /></button>
                      <button className={`text-[11px] font-semibold px-2 py-0.5 rounded ${r.active ? "text-[#E83B28]" : "text-emerald-700"}`}>
                        {r.active ? "Deactivate" : "Activate"}
                      </button>
                    </div></Td>
                  </tr>
                );
              })}
            </tbody>
          </TableWrapper>
        </Card>
      )}

      {tab === "vouchers" && (
        <Card>
          <FilterBar>
            <FilterSelect value="ALL" onChange={() => {}} options={[["ALL","All Milestones"],...MILESTONES.map(m=>[m.id,m.name] as [string,string])]} />
            <FilterSelect value="ALL" onChange={() => {}} options={[["ALL","All Status"],["AVAILABLE","Available"],["ASSIGNED","Assigned"],["EXPIRED","Expired"],["DISABLED","Disabled"]]} />
          </FilterBar>
          <TableWrapper>
            <thead><tr>
              <Th>Voucher Code</Th><Th>Milestone</Th><Th>Status</Th>
              <Th>Assigned User</Th><Th>Assigned At</Th><Th>Expires At</Th><Th>Actions</Th>
            </tr></thead>
            <tbody>
              {VOUCHERS.map((v) => (
                <tr key={v.id} className="hover:bg-[#F4F8FC]">
                  <Td>
                    <div className="flex items-center gap-2">
                      <code className="font-mono text-[11px] bg-[#F4F8FC] px-2 py-1 rounded border border-[#E4E7EC]">
                        {revealedVouchers.has(v.id) ? v.code : "••••-••••-••••"}
                      </code>
                      <button onClick={() => toggleReveal(v.id)} className="p-1 rounded hover:bg-[#F4F8FC] text-[#6B7280] hover:text-[#01599D]">
                        {revealedVouchers.has(v.id) ? <EyeOff size={12} /> : <Eye size={12} />}
                      </button>
                    </div>
                  </Td>
                  <Td className="text-xs text-[#6B7280]">{v.milestone}</Td>
                  <Td><Badge status={v.status} dot /></Td>
                  <Td className="font-mono text-[11px] text-[#6B7280]">{v.userId ?? "—"}</Td>
                  <Td className="font-mono text-[11px] text-[#6B7280]">{v.assignedAt ?? "—"}</Td>
                  <Td className="font-mono text-[11px] text-[#6B7280]">{v.expiresAt}</Td>
                  <Td>
                    {v.status !== "DISABLED" && v.status !== "EXPIRED" &&
                      <button className="text-[11px] font-semibold text-[#E83B28] hover:underline">Disable</button>}
                  </Td>
                </tr>
              ))}
            </tbody>
          </TableWrapper>
          <Pagination shown={VOUCHERS.length} total={VOUCHERS.length} />
        </Card>
      )}

      {tab === "import" && (
        <div className="max-w-xl">
          <Card className="p-5 space-y-4">
            <AlertBanner type="info">Voucher codes are stored as-is. Ensure codes match the partner's format exactly. Duplicates are detected and rejected automatically.</AlertBanner>
            <SelectField label="Milestone" value="" onChange={() => {}} required options={MILESTONES.map(m=>({label:m.name,value:m.id}))} />
            <div className="flex flex-col gap-1">
              <label className="text-xs font-semibold text-[#6B7280] uppercase tracking-wide">Voucher Codes <span className="text-[#E83B28]">*</span></label>
              <textarea rows={7} placeholder={"HC-2024-JUN-0001\nHC-2024-JUN-0002\nHC-2024-JUN-0003"}
                className="px-3 py-2 text-sm font-mono bg-white border border-[#E4E7EC] rounded focus:outline-none focus:ring-2 focus:ring-[#01599D] resize-none text-[#1D2433] leading-relaxed" />
              <p className="text-[11px] text-[#6B7280]">One code per line · Duplicates are auto-rejected</p>
            </div>
            <Input label="Expires At" value="" onChange={() => {}} type="date" required hint="All imported codes will share this expiry date" />
            <Btn variant="primary" size="md" onClick={() => setImportDone(true)}><Download size={13} /> Import Vouchers</Btn>
            {importDone && (
              <div className="grid grid-cols-3 gap-3 pt-1">
                {[["Imported","42","bg-emerald-50 text-emerald-700 border-emerald-200"],["Duplicate","3","bg-amber-50 text-amber-700 border-amber-200"],["Rejected","0","bg-[#F4F8FC] text-[#6B7280] border-[#E4E7EC]"]].map(([l,v,c])=>(
                  <div key={l} className={`rounded border px-3 py-2.5 text-center ${c}`}>
                    <p className="text-xl font-bold font-mono">{v}</p>
                    <p className="text-[11px] font-semibold uppercase tracking-wide">{l}</p>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>
      )}

      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Create Reward" subtitle="Rewards are issued when a user reaches a milestone">
        <DrawerSection title="Assignment" />
        <SelectField label="Milestone" value="" onChange={() => {}} required options={MILESTONES.map(m=>({label:m.name,value:m.id}))} />
        <SelectField label="Partner" value="" onChange={() => {}} options={[{label:"(No partner — internal reward)",value:""},...PARTNERS.map(p=>({label:p.name,value:p.id}))]} />
        <DrawerSection title="Reward Details" />
        <SelectField label="Reward Type" value="VOUCHER" onChange={() => {}} required options={[{label:"Voucher",value:"VOUCHER"},{label:"Digital Sticker",value:"DIGITAL_STICKER"},{label:"Bonus Stamp",value:"BONUS_STAMP"}]} />
        <Input label="Name" placeholder="Coffee Voucher 20k" value="" onChange={() => {}} required />
        <Input label="Description" placeholder="Redeem at Highlands Coffee…" value="" onChange={() => {}} />
        <DrawerSection title="Stock & Expiry" />
        <div className="grid grid-cols-2 gap-3">
          <Input label="Value (₫)" placeholder="20000" value="" onChange={() => {}} type="number" hint="0 for non-monetary rewards" />
          <Input label="Expiry Days" placeholder="30" value="" onChange={() => {}} type="number" hint="Days from issue date" />
        </div>
        <Input label="Total Stock" placeholder="500" value="" onChange={() => {}} type="number" hint="Maximum vouchers that can be issued" />
      </Drawer>
    </div>
  );
}

// ─── Analytics ────────────────────────────────────────────────────────────────

function AnalyticsScreen() {
  const readinessIssues = notReadyStations;
  return (
    <div className="space-y-5">
      <div className="grid grid-cols-5 gap-3">
        {[
          { label: "Total Stamps Collected",  value: "17,445", sub: "+342 last 7d",        icon: Stamp,      color: "#01599D" },
          { label: "Active Campaigns",         value: "2",      sub: "1 draft pending",     icon: Megaphone,  color: "#8B5CF6" },
          { label: "Voucher Availability",     value: "83%",    sub: `${lowStockRewards.length} low stock`,  icon: Ticket,     color: lowStockRewards.length > 0 ? "#E83B28" : "#10B981" },
          { label: "Top Station (Stamps)",     value: "Bến Thành", sub: "4,821 collectors", icon: TrendingUp, color: "#009B3A" },
          { label: "Stamps Today",             value: "128",    sub: "as of 14:32",         icon: Activity,   color: "#F59E0B" },
        ].map(({ label, value, sub, icon: Icon, color }) => (
          <Card key={label} className="p-4">
            <div className="flex items-start justify-between mb-2">
              <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest">{label}</p>
              <div className="p-1.5 rounded" style={{ backgroundColor: color + "18" }}>
                <Icon size={13} style={{ color }} />
              </div>
            </div>
            <p className="text-2xl font-bold text-[#1D2433]">{value}</p>
            <p className="text-[11px] text-[#6B7280] mt-0.5">{sub}</p>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Card className="p-5">
          <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-4">Stamps per Campaign</p>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={STAMPS_PER_CAMPAIGN} barSize={32}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E4E7EC" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 11, fill: "#6B7280" }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 11, fill: "#6B7280" }} axisLine={false} tickLine={false} />
              <Tooltip contentStyle={{ fontSize: 12, border: "1px solid #E4E7EC", borderRadius: 6 }} />
              <Bar dataKey="stamps" fill="#01599D" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>
        <Card className="p-5">
          <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-4">Station Collector Ranking (Top 8)</p>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={STATIONS.slice(0,8).map(s=>({ name: s.name.split(" ").pop(), collectors: s.collectors }))} layout="vertical" barSize={14}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E4E7EC" horizontal={false} />
              <XAxis type="number" tick={{ fontSize: 10, fill: "#6B7280" }} axisLine={false} tickLine={false} />
              <YAxis type="category" dataKey="name" tick={{ fontSize: 10, fill: "#6B7280" }} axisLine={false} tickLine={false} width={68} />
              <Tooltip contentStyle={{ fontSize: 12, border: "1px solid #E4E7EC", borderRadius: 6 }} />
              <Bar dataKey="collectors" fill="#009B3A" radius={[0, 3, 3, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>
      </div>

      <Card>
        <div className="flex items-center justify-between px-4 py-3 border-b border-[#E4E7EC]">
          <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest">Station Operational Readiness</p>
          <div className="flex items-center gap-2">
            <Badge status="READY" label={`${readyStations.length} ready`} dot />
            <Badge status="NOT_READY" label={`${readinessIssues.length} not ready`} dot />
          </div>
        </div>
        <TableWrapper>
          <thead><tr><Th>Station</Th><Th>Code</Th><Th>Line</Th><Th>GPS</Th><Th>Scan Key</Th><Th>Readiness</Th><Th>Blocking Issue</Th></tr></thead>
          <tbody>
            {readinessIssues.map((s) => (
              <tr key={s.id} className="hover:bg-[#F4F8FC] bg-[#FDEDEB]/10">
                <Td className="font-semibold">{s.name}</Td>
                <Td><MonoCode>{s.code}</MonoCode></Td>
                <Td><span className="px-1.5 py-0.5 text-[11px] font-bold rounded text-white" style={{ backgroundColor: s.lineColor }}>{s.line}</span></Td>
                <Td>{s.gpsReady ? <Badge status="GPS_OK" dot /> : <Badge status="GPS_MISSING" dot />}</Td>
                <Td>{s.scanKeyStatus === "CONFIGURED" ? <Badge status="SCAN_KEY_OK" dot /> : <Badge status="SCAN_KEY_MISSING" dot />}</Td>
                <Td><ReadinessBadge gpsReady={s.gpsReady} scanKeyConfigured={s.scanKeyStatus === "CONFIGURED"} /></Td>
                <Td>
                  <span className="text-[11px] text-[#E83B28] font-medium flex items-center gap-1">
                    <AlertTriangle size={11} />
                    {!s.gpsReady && s.scanKeyStatus === "MISSING" ? "GPS coordinates and scan key missing"
                    : !s.gpsReady ? "GPS coordinates not set — geofence inactive"
                    : "Scan key not configured — scans will be rejected"}
                  </span>
                </Td>
              </tr>
            ))}
          </tbody>
        </TableWrapper>
      </Card>
    </div>
  );
}

// ─── RBAC ─────────────────────────────────────────────────────────────────────

function RBACScreen() {
  const [tab, setTab] = useState<"roles"|"permissions"|"users">("roles");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [systemRoleWarn, setSystemRoleWarn] = useState(false);
  const [revokeConfirm, setRevokeConfirm] = useState<string | null>(null);

  return (
    <div>
      <AlertBanner type="info">
        Role and permission changes take effect immediately. Modifications to system roles affect all users with that role across all sessions.
      </AlertBanner>
      <div className="flex items-center gap-1 mb-4 border-b border-[#E4E7EC]">
        {(["roles","permissions","users"] as const).map((t) => (
          <button key={t} onClick={() => setTab(t)} className={`px-4 py-2.5 text-xs font-bold uppercase tracking-wide border-b-2 transition-colors ${tab===t?"border-[#01599D] text-[#01599D]":"border-transparent text-[#6B7280] hover:text-[#1D2433]"}`}>
            {t === "users" ? "User Role Assignment" : t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {tab === "roles" && (
        <Card>
          <TableWrapper>
            <thead><tr><Th>Role Name</Th><Th>Description</Th><Th>Status</Th><Th>System Role</Th><Th>Actions</Th></tr></thead>
            <tbody>
              {ROLES.map((r) => (
                <tr key={r.id} className={`hover:bg-[#F4F8FC] ${r.isSystem ? "bg-[#F4F8FC]/50" : ""}`}>
                  <Td>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-[#1D2433]">{r.name}</span>
                      {r.isSystem && <Shield size={12} className="text-[#01599D]" />}
                    </div>
                  </Td>
                  <Td className="text-xs text-[#6B7280]">{r.description}</Td>
                  <Td><Badge status={r.status} dot /></Td>
                  <Td>
                    {r.isSystem
                      ? <span className="text-[11px] font-bold text-[#01599D] bg-[#F4F8FC] border border-blue-200 px-2 py-0.5 rounded">System-defined</span>
                      : <span className="text-[11px] text-[#6B7280]">Custom</span>}
                  </Td>
                  <Td>
                    <div className="flex gap-1 items-center">
                      <button onClick={() => { if (r.isSystem) setSystemRoleWarn(true); else setDrawerOpen(true); }} className="p-1 rounded hover:bg-[#E4E7EC] text-[#6B7280] hover:text-[#01599D]"><Pencil size={13} /></button>
                      <button className="text-[11px] font-semibold text-[#6B7280] hover:text-[#01599D] px-2 py-0.5 rounded hover:bg-[#F4F8FC]">Assign Permissions</button>
                    </div>
                  </Td>
                </tr>
              ))}
            </tbody>
          </TableWrapper>
        </Card>
      )}

      {tab === "permissions" && (
        <Card>
          <TableWrapper>
            <thead><tr><Th>Permission Key</Th><Th>Description</Th></tr></thead>
            <tbody>
              {PERMISSIONS.map((p) => (
                <tr key={p.id} className="hover:bg-[#F4F8FC]">
                  <Td><MonoCode>{p.name}</MonoCode></Td>
                  <Td className="text-xs text-[#6B7280]">{p.description}</Td>
                </tr>
              ))}
            </tbody>
          </TableWrapper>
        </Card>
      )}

      {tab === "users" && (
        <div className="max-w-lg space-y-4">
          <Card className="p-5">
            <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-3">Lookup User</p>
            <div className="flex gap-2">
              <input placeholder="User ID (e.g. usr_7f2a)" className="flex-1 px-3 py-2 text-sm font-mono bg-white border border-[#E4E7EC] rounded focus:outline-none focus:ring-2 focus:ring-[#01599D]" />
              <Btn variant="secondary" size="sm">Lookup</Btn>
            </div>
          </Card>
          <Card className="p-5">
            <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-1">Assigned Roles</p>
            <p className="text-xs text-[#6B7280] font-mono mb-3">usr_7f2a</p>
            <div className="space-y-2 mb-4">
              {["Operator","Analyst"].map((role) => (
                <div key={role} className="flex items-center justify-between py-2 px-3 bg-[#F4F8FC] rounded border border-[#E4E7EC]">
                  <span className="text-sm font-semibold text-[#1D2433]">{role}</span>
                  <button onClick={() => setRevokeConfirm(role)} className="text-[11px] text-[#E83B28] font-bold hover:underline">Revoke</button>
                </div>
              ))}
            </div>
            <div className="flex gap-2">
              <select className="flex-1 px-3 py-2 text-sm bg-white border border-[#E4E7EC] rounded focus:outline-none">
                {ROLES.map(r => <option key={r.id}>{r.name}</option>)}
              </select>
              <Btn variant="primary" size="sm">Assign Role</Btn>
            </div>
          </Card>
        </div>
      )}

      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} title="Edit Role" subtitle="Changes affect all users with this role immediately">
        <DrawerSection title="Role" />
        <Input label="Role Name" value="Operator" onChange={() => {}} required />
        <Input label="Description" value="Manage stations and campaigns" onChange={() => {}} />
        <DrawerSection title="Status" />
        <SelectField label="Status" value="ACTIVE" onChange={() => {}} options={[{label:"Active",value:"ACTIVE"},{label:"Inactive",value:"INACTIVE"}]} />
      </Drawer>

      <ConfirmModal open={systemRoleWarn} onClose={() => setSystemRoleWarn(false)} onConfirm={() => setDrawerOpen(true)}
        title="Editing a System-Defined Role"
        message="This role is system-defined and cannot be safely renamed. Modifying its permissions will immediately affect all users with this role, including super admins. Proceed only if you are certain of the change."
        confirmLabel="Edit Anyway" dangerous />
      <ConfirmModal open={!!revokeConfirm} onClose={() => setRevokeConfirm(null)} onConfirm={() => {}}
        title={`Revoke Role — ${revokeConfirm}`}
        message={`Revoking "${revokeConfirm}" from user usr_7f2a will take effect immediately. The user will lose all access rights granted by this role on their next API call.`}
        confirmLabel="Revoke Role" dangerous />
    </div>
  );
}

// ─── Settings ─────────────────────────────────────────────────────────────────

function SettingsScreen({ onLogout }: { onLogout: () => void }) {
  const [envConfirm, setEnvConfirm] = useState(false);
  const [pendingEnv, setPendingEnv] = useState("");

  return (
    <div className="max-w-xl space-y-4">
      <Card className="p-5">
        <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-4">Admin Profile</p>
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-full bg-[#01599D] flex items-center justify-center text-white text-base font-bold">NA</div>
          <div>
            <p className="text-sm font-bold text-[#1D2433]">Nguyen Admin</p>
            <p className="text-xs text-[#6B7280] font-mono">admin@exoticstamp.vn</p>
            <div className="mt-1"><Badge status="ACTIVE" label="Super Admin" dot /></div>
          </div>
        </div>
      </Card>

      <Card className="p-5">
        <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-3">API Environment</p>
        <div className="flex items-center gap-3 mb-3">
          <span className="text-sm text-[#6B7280]">Current:</span>
          <span className="px-2 py-0.5 text-[11px] font-bold bg-amber-50 text-amber-700 border border-amber-200 rounded uppercase">Staging</span>
        </div>
        <div className="flex items-center gap-3">
          <select defaultValue="STAGING"
            onChange={(e) => { if (e.target.value === "PRODUCTION") { setPendingEnv("PRODUCTION"); setEnvConfirm(true); } }}
            className="px-3 py-2 text-sm bg-white border border-[#E4E7EC] rounded focus:outline-none">
            <option value="LOCAL">Local (localhost:3000)</option>
            <option value="STAGING">Staging (api-staging.exoticstamp.vn)</option>
            <option value="PRODUCTION">Production (api.exoticstamp.vn)</option>
          </select>
        </div>
        <p className="text-[11px] text-[#E83B28] mt-2 flex items-center gap-1"><AlertTriangle size={11} /> Switching to Production affects all live users. A confirmation is required.</p>
      </Card>

      <Card className="p-5">
        <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-3">Session</p>
        <KV label="Signed in as" value={<span className="font-mono text-xs">admin@exoticstamp.vn</span>} />
        <KV label="Session started" value="Today at 09:14" />
        <KV label="Last activity" value="14:32" />
        <div className="pt-3">
          <Btn variant="danger" size="sm" onClick={onLogout}><LogOut size={13} /> Sign Out</Btn>
        </div>
      </Card>

      <Card className="p-5">
        <p className="text-[10px] font-bold text-[#6B7280] uppercase tracking-widest mb-3">About</p>
        <KV label="Application" value="Exotic Stamp Admin Console" />
        <KV label="Version" value={<MonoCode>v1.0.0-beta</MonoCode>} />
        <KV label="Build date" value={<MonoCode>2024-06-25</MonoCode>} />
        <KV label="API" value={<MonoCode>api-staging.exoticstamp.vn</MonoCode>} />
      </Card>

      <ConfirmModal open={envConfirm} onClose={() => setEnvConfirm(false)} onConfirm={() => {}}
        title="Switch to Production Environment"
        message="You are about to point this admin console at the Production API. All create, edit, and delete actions will immediately affect live data and real mobile users. This change applies to your current session only."
        confirmLabel="Switch to Production" dangerous />
    </div>
  );
}

// ─── Page titles ──────────────────────────────────────────────────────────────

const PAGE_TITLES: Record<Screen, string> = {
  login: "Login", dashboard: "Dashboard", "metro-lines": "Metro Lines",
  "metro-line-detail": "Metro Line Detail", stations: "Stations",
  "station-detail": "Station Detail", campaigns: "Campaigns",
  "campaign-detail": "Campaign Detail", "stamp-designs": "Stamp Designs",
  partners: "Partners", milestones: "Milestones", rewards: "Rewards & Vouchers",
  analytics: "Analytics", rbac: "RBAC", settings: "Settings",
};

// ─── App root ─────────────────────────────────────────────────────────────────

export default function App() {
  const [loggedIn, setLoggedIn] = useState(false);
  const [screen, setScreen] = useState<Screen>("dashboard");
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  if (!loggedIn) return <LoginScreen onLogin={() => setLoggedIn(true)} />;

  const navigate = (s: Screen) => setScreen(s);

  return (
    <div className="flex h-screen overflow-hidden bg-[#F4F8FC]" style={{ fontFamily: "'Inter', system-ui, sans-serif" }}>
      <Sidebar current={screen} onNavigate={navigate} collapsed={sidebarCollapsed} onToggle={() => setSidebarCollapsed(!sidebarCollapsed)} />
      <div className="flex flex-col flex-1 min-w-0 overflow-hidden">
        <Header title={PAGE_TITLES[screen]} onLogout={() => setLoggedIn(false)} />
        <main className="flex-1 overflow-y-auto p-6">
          <div className="max-w-[1200px] mx-auto">
            {screen === "dashboard"        && <DashboardScreen onNavigate={navigate} />}
            {screen === "metro-lines"      && <MetroLinesScreen onViewDetail={() => navigate("metro-line-detail")} />}
            {screen === "metro-line-detail"&& (
              <div>
                <Breadcrumb items={["Metro Lines","Metro Line 1"]} />
                <p className="text-sm text-[#6B7280]">Full detail page — mirrors Station Detail pattern.</p>
                <div className="mt-3"><Btn variant="secondary" size="sm" onClick={() => navigate("metro-lines")}><ChevronLeft size={13} /> Back to Metro Lines</Btn></div>
              </div>
            )}
            {screen === "stations"         && <StationsScreen onViewDetail={() => navigate("station-detail")} />}
            {screen === "station-detail"   && <StationDetailScreen />}
            {screen === "campaigns"        && <CampaignsScreen onViewDetail={() => navigate("campaign-detail")} />}
            {screen === "campaign-detail"  && <CampaignDetailScreen />}
            {screen === "stamp-designs"    && <StampDesignsScreen />}
            {screen === "partners"         && <PartnersScreen />}
            {screen === "milestones"       && <MilestonesScreen />}
            {screen === "rewards"          && <RewardsScreen />}
            {screen === "analytics"        && <AnalyticsScreen />}
            {screen === "rbac"             && <RBACScreen />}
            {screen === "settings"         && <SettingsScreen onLogout={() => setLoggedIn(false)} />}
          </div>
        </main>
      </div>
    </div>
  );
}
