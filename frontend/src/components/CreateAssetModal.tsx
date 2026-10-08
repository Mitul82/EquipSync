import React from 'react';
import { X, Plus, Monitor, Hash } from 'lucide-react';

import useCreateAsset from '@/hooks/useCreateAsset';

type AssetStatus = 'Available' | 'Assigned' | 'In_repair' | 'Retired';

interface CreateAssetModalProps {
    onClose: () => void;
}

const statusOptions: { value: AssetStatus; label: string }[] = [
    { value: 'Available', label: 'Available' },
    { value: 'In_repair', label: 'In Repair' },
    { value: 'Retired', label: 'Retired' },
];

const inputClass = 'w-full rounded-xl border bg-background py-3 pl-10 pr-4 text-sm text-neutral-900 placeholder:text-neutral-400 transition-all focus:border-transparent focus:outline-none focus:ring-2 disabled:opacity-60';
const labelClass = 'mb-2 block text-xs font-bold uppercase tracking-wider text-secondary';

function CreateAssetModal({ onClose }: CreateAssetModalProps) {
    const [name, setName] = React.useState<string>('');
    const [serialNumber, setSerialNumber] = React.useState<string>('');
    const [status, setStatus] = React.useState<AssetStatus>('Available');
    const [submitted, setSubmitted] = React.useState<boolean>(false);

    const { mutate: createAsset, isPending } = useCreateAsset();

    const nameError = submitted && !name.trim() ? 'Asset name is required.' : '';
    const serialError = submitted && !serialNumber.trim() ? 'Serial number is required.' : '';

    React.useEffect(() => {
        const onKey = (e: KeyboardEvent) => e.key === 'Escape' && !isPending && onClose();
        window.addEventListener('keydown', onKey);

        const previous = document.body.style.overflow;
        document.body.style.overflow = 'hidden';

        return () => {
            window.removeEventListener('keydown', onKey);
            document.body.style.overflow = previous;
        };
    }, [isPending, onClose]);

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setSubmitted(true);

        if (!name.trim() || !serialNumber.trim()) return;

        createAsset(
            { name: name.trim(), serialNumber: serialNumber.trim(), status },
            { onSuccess: onClose }
        );
    };

    return (
        <div className='fixed inset-0 z-50 flex items-center justify-center p-4' role='dialog' aria-modal='true' aria-labelledby='create-asset-title'>
            <div onClick={() => !isPending && onClose()} aria-hidden='true' className='absolute inset-0 bg-neutral-900/40 backdrop-blur-sm' />

            <div className='relative max-h-[90vh] w-full max-w-md overflow-y-auto rounded-3xl border border-neutral-200 bg-background p-6 shadow-xl sm:p-8'>
                <div className='flex items-start justify-between gap-4'>
                    <div>
                        <h2 id='create-asset-title' className='text-sm font-bold uppercase tracking-wider text-neutral-900'>Add Asset</h2>
                        <p className='mt-1 text-xs text-neutral-500'>Register a new piece of equipment.</p>
                    </div>
                    <button type='button' onClick={onClose} disabled={isPending} aria-label='Close' className='rounded-full p-2 text-neutral-400 transition-colors hover:bg-neutral-100 hover:text-neutral-700 disabled:opacity-60'>
                        <X className='h-4 w-4' />
                    </button>
                </div>

                <form onSubmit={handleSubmit} noValidate className='mt-6 space-y-5'>
                    <div>
                        <label htmlFor='asset-name' className={labelClass}>Asset Name</label>
                        <div className='relative'>
                            <div className='pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-neutral-400'>
                                <Monitor className='h-4 w-4' />
                            </div>
                            <input id='asset-name' type='text' value={name} onChange={(e) => setName(e.target.value)} disabled={isPending} placeholder='e.g. Dell UltraSharp 27" Monitor' aria-invalid={!!nameError} className={`${inputClass} ${nameError ? 'border-red-300 focus:ring-red-400/80' : 'border-neutral-200 focus:ring-primary/80'}`}/>
                        </div>
                        {nameError && <p role='alert' className='mt-2 text-xs font-medium text-red-600'>{nameError}</p>}
                    </div>

                    <div>
                        <label htmlFor='asset-serial' className={labelClass}>Serial Number</label>
                        <div className='relative'>
                            <div className='pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-neutral-400'>
                                <Hash className='h-4 w-4' />
                            </div>
                            <input id='asset-serial' type='text' value={serialNumber} onChange={(e) => setSerialNumber(e.target.value)} disabled={isPending} placeholder='e.g. SN-48210-XK' aria-invalid={!!serialError} className={`${inputClass} font-mono ${serialError ? 'border-red-300 focus:ring-red-400/80' : 'border-neutral-200 focus:ring-primary/80'}`}/>
                        </div>
                        {serialError && <p role='alert' className='mt-2 text-xs font-medium text-red-600'>{serialError}</p>}
                    </div>

                    <div>
                        <label htmlFor='asset-status' className={labelClass}>Status</label>
                        <select id='asset-status' value={status} onChange={(e) => setStatus(e.target.value as AssetStatus)} disabled={isPending} className='w-full cursor-pointer rounded-xl border border-neutral-200 bg-background px-4 py-3 text-sm text-neutral-900 transition-all focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary/80 disabled:opacity-60'>
                            {statusOptions.map((opt) => (
                                <option key={opt.value} value={opt.value}>{opt.label}</option>
                            ))}
                        </select>
                    </div>

                    <div className='flex flex-col-reverse gap-3 pt-2 sm:flex-row'>
                        <button type='button' onClick={onClose} disabled={isPending} className='flex-1 cursor-pointer rounded-full border border-neutral-200 py-3 text-xs font-bold uppercase tracking-widest text-neutral-600 transition-all hover:bg-neutral-100 disabled:cursor-not-allowed disabled:opacity-60'>
                            Cancel
                        </button>
                        <button type='submit' disabled={isPending} className='inline-flex flex-1 cursor-pointer items-center justify-center gap-2 rounded-full bg-primary py-3 text-xs font-bold uppercase tracking-widest text-background shadow-md transition-all hover:bg-primary/90 hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-60'>
                            {isPending ? 'Creating...' : <>Create Asset <Plus className='h-4 w-4' /></>}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default CreateAssetModal;