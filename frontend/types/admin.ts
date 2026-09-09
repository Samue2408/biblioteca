export type AdminStatsResponse = {
  totalBooks: number;
  availableBooks: number;
  borrowedBooks: number;
  reservedBooks: number;
  totalLoans: number;
  activeLoans: number;
  overdueLoans: number;
};
