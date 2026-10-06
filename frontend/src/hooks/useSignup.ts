import { toast } from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';

import api from '@/utils/api';

import type { AxiosError } from 'axios';
import type { ApiRes, AuthUser } from '@/types';

type Credentials = {
    email: string,
    password: string,
    department: string,
}

function useSignup() {
    const navigate = useNavigate();

    return useMutation({
        mutationFn: async (credentials: Credentials) => {
            const { data }: { data: ApiRes<AuthUser> } = await api.post('/auth/signup', credentials);

            return data;
        },
        onMutate: () => {
            toast.loading('Signing up...', { id: 'signup-toast' });
        },
        onSuccess: (data: ApiRes<AuthUser>) => {
            toast.success(data.message || 'Signed up', { id: 'signup-toast' });

            if(data?.data?.role === 'Admin') {
                navigate('/admin');
            } else if (data?.data?.role === 'Manager') {
                navigate('/manager');
            } else if(data?.data?.role === 'Employee') {
                navigate('/dashboard');
            } else {
                navigate('/');
            }
        },
        onError: (err: AxiosError<{ message: string }>) => {
            const errMessage = err.response?.data?.message || 'An Error Occured';

            toast.error(errMessage, { id: 'signup-toast' });
        }
    });
}

export default useSignup;