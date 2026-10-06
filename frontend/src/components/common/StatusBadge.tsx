import type { UserAsset, UserRequests } from "@/types";

type RequestStatus = UserRequests[number]['status'];
type AssetStatus = UserAsset['status'];

const styles: Record<RequestStatus | AssetStatus, { badge: string; dot: string; label: string }> = {
    Pending: { badge: 'bg-amber-50 text-amber-700 border-amber-200', dot: 'bg-amber-500', label: 'Pending' },
    Approved: { badge: 'bg-blue-50 text-blue-700 border-blue-200', dot: 'bg-blue-500', label: 'Approved' },
    Denied: { badge: 'bg-red-50 text-red-700 border-red-200', dot: 'bg-red-500', label: 'Denied' },
    Fullfilled: { badge: 'bg-emerald-50 text-emerald-700 border-emerald-200', dot: 'bg-emerald-500', label: 'Fulfilled' },
    Available: { badge: 'bg-emerald-50 text-emerald-700 border-emerald-200', dot: 'bg-emerald-500', label: 'Available' },
    Assigned: { badge: 'bg-blue-50 text-blue-700 border-blue-200', dot: 'bg-blue-500', label: 'Assigned' },
    In_repair: { badge: 'bg-amber-50 text-amber-700 border-amber-200', dot: 'bg-amber-500', label: 'In Repair' },
    Retired: { badge: 'bg-neutral-100 text-neutral-600 border-neutral-200', dot: 'bg-neutral-400', label: 'Retired' },
}

function StatusBadge({ status }: { status: RequestStatus | AssetStatus }) {
    const s = styles[status];
    const pulse = status === 'Pending' || status === 'In_repair';

    return (
        <span className={`inline-flex shrink-0 items-center gap-2 rounded-full border px-3 py-1 text-xs font-bold uppercase tracking-wider ${s.badge}`}>
            <span className='relative flex h-2 w-2'>
                {pulse && <span className={`absolute inline-flex h-full w-full animate-ping rounded-full opacity-75 ${s.dot}`} />}
                <span className={`relative inline-flex h-2 w-2 rounded-full ${s.dot}`} />
            </span>
            {s.label}
        </span>
    );
}

export default StatusBadge;