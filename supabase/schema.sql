-- Fitcrave Supabase schema
-- Paste this into the Supabase SQL editor for your project.

-- 1. profiles
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text,
  email text,
  created_at timestamptz default now()
);

alter table public.profiles enable row level security;

create policy "Users can read own profile"
  on public.profiles for select
  using (auth.uid() = id);

create policy "Users can insert own profile"
  on public.profiles for insert
  with check (auth.uid() = id);

create policy "Users can update own profile"
  on public.profiles for update
  using (auth.uid() = id);

-- 2. daily_stats
create table if not exists public.daily_stats (
  id bigserial primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  day_date date not null,
  workouts_completed int default 0,
  kcal int default 0,
  minutes int default 0,
  unique (user_id, day_date)
);

alter table public.daily_stats enable row level security;

create policy "Users can read own stats"
  on public.daily_stats for select
  using (auth.uid() = user_id);

create policy "Users can insert own stats"
  on public.daily_stats for insert
  with check (auth.uid() = user_id);

create policy "Users can update own stats"
  on public.daily_stats for update
  using (auth.uid() = user_id);

-- 3. workouts (per-day completion log)
create table if not exists public.workouts (
  id bigserial primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  day_number int not null check (day_number between 1 and 7),
  completed boolean default true,
  created_at timestamptz default now()
);

alter table public.workouts enable row level security;

create policy "Users can read own workouts"
  on public.workouts for select
  using (auth.uid() = user_id);

create policy "Users can insert own workouts"
  on public.workouts for insert
  with check (auth.uid() = user_id);

create policy "Users can update own workouts"
  on public.workouts for update
  using (auth.uid() = user_id);
