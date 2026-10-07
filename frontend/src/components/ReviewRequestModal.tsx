import React from 'react';
import { X, Check, Ban, User, Calendar, Monitor } from 'lucide-react';

import StatusBadge from '@/components/StatusBadge';
import useGetAllAvailableAssets from '@/hooks/getAllAvailableAssets';

import type { UserRequests } from '@/types';

type Request = UserRequests[number];

interface ReviewRequestModalProps {
    request: Request | null;
    isSubmitting: boolean;
    onClose: () => void;
    onApprove: (assetId: string) => void;
    onDeny: () => void;
}

const formatDate = (iso: string) =>
    new Date(iso).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });

function ReviewRequestModal({ request, isSubmitting, onClose, onApprove, onDeny }: ReviewRequestModalProps) {
    const [assetId, setAssetId] = React.useState<string>('');
    const { data: availableAssets, isPending: loadingAssets } = useGetAllAvailableAssets();

    const assets = availableAssets?.data || [];

    React.useEffect(() => {
        setAssetId('');
    }, [request?.id]);

    React.useEffect(() => {
        if (!request) return;

        const onKey = (e: KeyboardEvent) => e.key === 'Escape' && !isSubmitting && onClose();
        window.addEventListener('keydown', onKey);

        const previous = document.body.style.overflow;
        document.body.style.overflow = 'hidden';

        return () => {
            window.removeEventListener('keydown', onKey);
            document.body.style.overflow = previous;
        };
    }, [request, isSubmitting, onClose]);

    if (!request) return null;

    return (
        <div className='fixed inset-0 z-50 flex items-center justify-center p-4' role='dialog' aria-modal='true' aria-labelledby='review-title'>
            <div onClick={() => !isSubmitting && onClose()} aria-hidden='true' className='absolute inset-0 bg-neutral-900/40 backdrop-blur-sm' />

            <div className='relative max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-3xl border border-neutral-200 bg-background p-6 shadow-xl sm:p-8'>
                <div className='flex items-start justify-between gap-4'>
                    <div>
                        <h2 id='review-title' className='text-sm font-bold uppercase tracking-wider text-neutral-900'>Review Request</h2>
                        <p className='mt-1 text-xs text-neutral-500'>Assign an asset to approve, or deny the request.</p>
                    </div>
                    <button type='button' onClick={onClose} disabled={isSubmitting} aria-label='Close' className='rounded-full p-2 text-neutral-400 transition-colors hover:bg-neutral-100 hover:text-neutral-700 disabled:opacity-60'>
                        <X className='h-4 w-4' />
                    </button>
                </div>

                <div className='mt-6 space-y-5'>
                    <div>
                        <p className='mb-1.5 text-xs font-bold uppercase tracking-wider text-neutral-400'>Requested By</p>
                        <div className='flex items-center gap-3'>
                            <div className='rounded-full bg-neutral-100 p-2 text-neutral-500'>
                                <User className='h-4 w-4' />
                            </div>
                            <div className='min-w-0'>
                                <p className='truncate text-sm font-medium text-neutral-900'>{request.requester.email}</p>
                                <p className='text-xs text-neutral-500'>{request.requester.department} · {request.requester.role}</p>
                            </div>
                        </div>
                    </div>

                    <div>
                        <p className='mb-1.5 text-xs font-bold uppercase tracking-wider text-neutral-400'>Description</p>
                        <div className='max-h-48 overflow-y-auto whitespace-pre-wrap rounded-2xl border border-neutral-200 bg-neutral-50 p-4 text-sm leading-relaxed text-neutral-900'>
                            {request.description}
                        </div>
                    </div>

                    <div>
                        <label htmlFor='asset' className='mb-1.5 block text-xs font-bold uppercase tracking-wider text-neutral-400'>
                            Assign Asset
                        </label>
                        <div className='relative'>
                            <div className='pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-neutral-400'>
                                <Monitor className='h-4 w-4' />
                            </div>
                            <select id='asset' value={assetId} onChange={(e) => setAssetId(e.target.value)} disabled={isSubmitting || loadingAssets} className='w-full cursor-pointer appearance-none rounded-xl border border-neutral-200 bg-background py-3 pl-10 pr-4 text-sm text-neutral-900 transition-all focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary/80 disabled:opacity-60'>
                                <option value=''>
                                    {loadingAssets ? 'Loading assets...' : assets.length === 0 ? 'No available assets' : 'Select an available asset'}
                                </option>
                                {assets.map((a) => (
                                    <option key={a.id} value={a.id}>
                                        {a.name} · {a.serialNumber}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <p className='mt-1.5 text-xs text-neutral-400'>Only needed when approving.</p>
                    </div>

                    <div className='flex items-center justify-between'>
                        <p className='inline-flex items-center gap-1.5 text-xs text-neutral-500'>
                            <Calendar className='h-3.5 w-3.5' />
                            Requested on {formatDate(request.createdAt)}
                        </p>
                        <StatusBadge status={request.status} />
                    </div>
                </div>

                <div className='mt-8 flex flex-col-reverse gap-3 sm:flex-row'>
                    <button type='button' onClick={onDeny} disabled={isSubmitting} className='inline-flex flex-1 cursor-pointer items-center justify-center gap-2 rounded-full border border-red-200 bg-red-50 py-3 text-xs font-bold uppercase tracking-widest text-red-700 transition-all hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-60'>
                        <Ban className='h-4 w-4' />
                        Deny
                    </button>
                    <button type='button' onClick={() => onApprove(assetId)} disabled={isSubmitting || !assetId} className='inline-flex flex-1 cursor-pointer items-center justify-center gap-2 rounded-full bg-primary py-3 text-xs font-bold uppercase tracking-widest text-background shadow-md transition-all hover:bg-primary/90 hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-60'>
                        <Check className='h-4 w-4' />
                        {isSubmitting ? 'Saving...' : 'Approve'}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default ReviewRequestModal;