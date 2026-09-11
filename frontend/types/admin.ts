export type AdminStatsResponse = {
  totalBooks: number;
  availableBooks: number;
  borrowedBooks: number;
  reservedBooks: number;
  totalLoans: number;
  activeLoans: number;
  overdueLoans: number;
  blockedUsers: number;
};

export type BlockedUser = {
  id: number;
  name: string;
  email: string;
  blockedUntil: string;
};