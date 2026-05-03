import { Component, type ReactNode } from "react";

type Props = { children: ReactNode; fallback?: (err: Error) => ReactNode };
type State = { error: Error | null };

export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error) {
    console.error("ErrorBoundary caught:", error);
  }

  render() {
    if (this.state.error) {
      if (this.props.fallback) return this.props.fallback(this.state.error);
      return (
        <div className="rounded-md border border-destructive/30 bg-destructive/5 p-4 text-destructive">
          <div className="font-semibold">Something went wrong</div>
          <pre className="mt-2 text-xs whitespace-pre-wrap">
            {this.state.error.message}
          </pre>
        </div>
      );
    }
    return this.props.children;
  }
}
