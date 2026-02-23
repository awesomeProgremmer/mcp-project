import { CommonModule } from "@angular/common";
import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { QueryResult } from "../../query.model";



@Component({
  selector: 'app-result-display',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './result-display.component.html',
  styleUrl: './result-display.component.scss'

})
export class ResultDisplayComponent implements OnChanges{

  @Input() result: QueryResult | null = null;

  @Input() error: string | null = null;

  @Input() isLoading: boolean = false;
  
  copied = false;
  animateIn = false;

ngOnChanges(changes: SimpleChanges): void {
  if(changes['result'] ?.currentValue ){
    this.animateIn = false;
    setTimeout(
      () => this.animateIn = true,10
    );
  }  
}


copySQL(): void {
  if(!this.result?.generatedSql) return;
  navigator.clipboard.writeText(this.result.generatedSql).then(
    () => {
      this.copied = true;
      setTimeout(() => this.copied = false, 2000);
    }
  )
}

get tableHeaders(): string[] {
if(!this.result?.data || this.result.data.length === 0) return [];
return Object.keys(this.result.data[0]);
 }



}