import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, ArrowRight, Lightbulb } from 'lucide-react';

import useCreateRequest from '@/hooks/useCreateRequest';

const MIN_LENGTH = 20;
const MAX_LENGTH = 500;

const hints = [
    { title: 'What', text: 'The type of equipment (e.g. laptop, monitor, keyboard).' },
    { title: 'How many', text: 'The quantity you need.' },
    { title: 'Why', text: 'The reason or project that needs it.' },
];

function CreateRequestPage() {
    const [description, setDescription] = React.useState<string>('');
    const [touched, setTouched] = React.useState<boolean>(false);

    const { mutate: createRequest, isPending } = useCreateRequest();

    const trimmed = description.trim();
    const error =
        touched && trimmed.length < MIN_LENGTH
            ? `Please add a little more detail (at least ${MIN_LENGTH} characters).`
            : '';

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setTouched(true);

        if (trimmed.length < MIN_LENGTH) return;

        createRequest(
            { description: trimmed },
            { onSuccess: () => { setDescription(''); setTouched(false); } }
        );
    };

    return (
        <div className='relative z-10 mx-auto max-w-2xl space-y-6'>
            <Link to='/dashboard/requests' className='inline-flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-neutral-400 transition-colors hover:text-neutral-900'>
                <ArrowLeft className='h-4 w-4' />
                Back to requests
            </Link>

            <div className='rounded-3xl border border-neutral-200 bg-background p-6 shadow-sm sm:p-8'>
                <h1 className='text-sm font-bold uppercase tracking-wider text-neutral-900'>New Equipment Request</h1>
                <p className='mt-1 text-xs text-neutral-500'>
                    Tell us what you need and a manager will review it.
                </p>

                <form onSubmit={handleSubmit} noValidate className='mt-6 space-y-5'>
                    <div>
                        <div className='mb-2 flex items-center justify-between'>
                            <label htmlFor='description' className='block text-xs font-bold uppercase tracking-wider text-secondary'>
                                Description
                            </label>
                            <span className={`text-xs font-medium ${description.length >= MAX_LENGTH ? 'text-red-600' : 'text-neutral-400'}`}>
                                {description.length}/{MAX_LENGTH}
                            </span>
                        </div>

                        <textarea
                            id='description'
                            name='description'
                            rows={7}
                            maxLength={MAX_LENGTH}
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            onBlur={() => setTouched(true)}
                            aria-invalid={!!error}
                            aria-describedby={error ? 'description-error' : undefined}
                            placeholder='e.g. I need 2 external monitors for the design team. We are working on dashboards all day and our current screens are too small to compare layouts side by side.'
                            className={`w-full resize-none rounded-xl border bg-background px-4 py-3 text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-transparent focus:outline-none focus:ring-2 transition-all ${
                                error ? 'border-red-300 focus:ring-red-400/80' : 'border-neutral-200 focus:ring-primary/80'
                            }`}
                        />

                        {error && (
                            <p id='description-error' role='alert' className='mt-2 text-xs font-medium text-red-600'>
                                {error}
                            </p>
                        )}
                    </div>

                    <div className='rounded-2xl border border-neutral-200 bg-neutral-50 p-4'>
                        <p className='mb-3 inline-flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-neutral-500'>
                            <Lightbulb className='h-4 w-4 text-primary' />
                            Include in your description
                        </p>
                        <ul className='space-y-2'>
                            {hints.map(({ title, text }) => (
                                <li key={title} className='flex gap-2 text-xs text-neutral-600'>
                                    <span className='w-16 shrink-0 font-bold uppercase tracking-wider text-neutral-900'>{title}</span>
                                    <span>{text}</span>
                                </li>
                            ))}
                        </ul>
                    </div>

                    <button type='submit' disabled={isPending} className='inline-flex w-full cursor-pointer items-center justify-center gap-2 rounded-full bg-primary py-3.5 text-xs font-bold uppercase tracking-widest text-background shadow-md transition-all duration-200 hover:bg-primary/90 hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-60'>
                        {isPending ? 'Submitting...' : 'Submit Request'}
                        {!isPending && <ArrowRight className='h-4 w-4' />}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default CreateRequestPage;